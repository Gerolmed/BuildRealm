package net.endrealm.lostsouls;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import fr.minuskube.inv.InventoryManager;
import lombok.Getter;
import net.endrealm.lostsouls.bridge.WorldEditListener;
import net.endrealm.lostsouls.chatinput.ChatInput;
import net.endrealm.lostsouls.chatinput.ChatInputManager;
import net.endrealm.lostsouls.commands.DraftCommand;
import net.endrealm.lostsouls.commands.ThemeCommand;
import net.endrealm.lostsouls.config.Configuration;
import net.endrealm.lostsouls.config.MainConfig;
import net.endrealm.lostsouls.gui.GuiService;
import net.endrealm.lostsouls.listener.ChatListener;
import net.endrealm.lostsouls.listener.EditWorldListener;
import net.endrealm.lostsouls.listener.LeaveListener;
import net.endrealm.lostsouls.listener.WorldChangeListener;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.repository.impl.BasicCache;
import net.endrealm.lostsouls.repository.impl.BasicDataProvider;
import net.endrealm.lostsouls.repository.impl.BasicDraftRepository;
import net.endrealm.lostsouls.repository.impl.BasicThemeRepository;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.PermissionService;
import net.endrealm.lostsouls.services.ThemeService;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.services.impl.BasicDraftService;
import net.endrealm.lostsouls.services.impl.BasicPermissionService;
import net.endrealm.lostsouls.services.impl.BasicThemeService;
import net.endrealm.lostsouls.services.impl.ThreadServiceImpl;
import net.endrealm.lostsouls.utils.Observable;
import net.endrealm.lostsouls.world.WorldIdentity;
import net.endrealm.lostsouls.world.WorldService;
import net.endrealm.lostsouls.world.impl.BasicWorldService;
import net.endrealm.lostsouls.world.impl.FileLoader;
import net.endrealm.lostsouls.world.impl.SlimeWorldAdapter;
import net.endrealm.realmdrive.factory.DriveServiceFactory;
import net.endrealm.realmdrive.interfaces.DriveService;
import net.endrealm.realmdrive.utils.DriveSettings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

@Getter
public final class LostSoulsSave extends JavaPlugin {

    private DataProvider dataProvider;
    private ThreadService threadService;
    private ThemeService themeService;
    private WorldService worldService;
    private DraftService draftService;
    private PermissionService permissionService;
    private ChatInputManager chatInputManager;
    private GuiService guiService;
    private final Long CACHE_DURATION = 150000L;
    private SlimePlugin slimePlugin;
    private WorldEditPlugin worldEditPlugin;
    private InventoryManager inventoryManager;
    private MainConfig mainConfig;
    private boolean running;
    private Observable<Boolean> isUILocked;

    @SuppressWarnings("BusyWait")
    @Override
    public void onEnable() {

        isUILocked = Observable.of(false);
        initConfigs();
        inventoryManager = new InventoryManager(this);
        inventoryManager.init();

        slimePlugin = (SlimePlugin) Bukkit.getServer().getPluginManager().getPlugin("SlimeWorldManager");
        worldEditPlugin = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        registerLoaders();

        threadService = new ThreadServiceImpl(this);

        // Plugin startup logic
        DriveSettings settings = DriveSettings.builder()
                .type(DriveSettings.BackendType.MONGO_DB)
                .hostURL("mongodb://localhost:27017")
                .database("soulssave")
                .table("primary")
                .build();

        DriveServiceFactory serviceFactory = new DriveServiceFactory();
        DriveService driveService = serviceFactory.getDriveService(settings);
        this.chatInputManager = new ChatInputManager();

        //TODO: add cache cleanup on iteration
        this.dataProvider = new BasicDataProvider(
                new BasicCache<>(CACHE_DURATION),
                new BasicDraftRepository(driveService),
                new BasicCache<>(CACHE_DURATION),
                new BasicThemeRepository(driveService)
        );
        this.worldService = new BasicWorldService<>(new SlimeWorldAdapter(slimePlugin, getSlimeLoader("openDrafts"), getSlimeLoader("closedDrafts")), threadService);
        this.themeService = new BasicThemeService(dataProvider, threadService);
        this.draftService = new BasicDraftService(dataProvider, threadService, worldService, themeService);
        this.permissionService = new BasicPermissionService(mainConfig, draftService);

        this.guiService = new GuiService(inventoryManager, draftService, threadService, themeService, dataProvider, chatInputManager, permissionService, isUILocked);
        registerCommands();
        registerEvents();

        this.worldEditPlugin.getWorldEdit().getEventBus().register(new WorldEditListener(dataProvider, worldService));

        startWorkers();
    }

    private void startWorkers() {
        threadService.runAsync(() -> {

            if(running) return;

            running = true;

            while (running) {
                try {
                    Thread.sleep(600000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                dataProvider.validateCaches();
            }
        });
        // Close all open UI's when locked
        isUILocked.subscribe(isLocked -> {
            if(!isLocked) return;
            threadService.runSync(() -> Bukkit.getOnlinePlayers().forEach(player -> inventoryManager.getInventory(player).ifPresent(smartInventory -> smartInventory.close(player))));
        });
        // Close all open worlds when ui is locked to prevent corrupt worlds on export
        isUILocked.subscribe(isLocked -> {
            if(!isLocked) return;
            Bukkit.getWorlds().forEach(world -> {
                WorldIdentity identity = new WorldIdentity(world.getName(), false);
                if(!worldService.isLoaded(identity))
                    return;
                worldService.unloadSync(identity);
            });
        });
    }

    private void initConfigs() {
        try {
            this.mainConfig = new MainConfig(new Configuration(this, "config.yml"));
        } catch (IOException e) {
            getLogger().severe("Failed to load config! Shutting down in 3 seconds to prevent any harm!");
            getLogger().severe("Delete the config and try again!!");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            System.exit(1);
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new WorldChangeListener(draftService, worldService), this);
        getServer().getPluginManager().registerEvents(new EditWorldListener(dataProvider, worldService), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(draftService, worldService, chatInputManager, this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(chatInputManager), this);

    }

    private void registerLoaders() {
        File parent = Bukkit.getWorldContainer();
        slimePlugin.registerLoader("openDrafts", new FileLoader(new File(parent, "openDrafts")));
        slimePlugin.registerLoader("closedDrafts", new FileLoader(new File(parent, "closedDrafts")));

    }

    private SlimeLoader getSlimeLoader(String loader) {
        return slimePlugin.getLoader(loader);
    }

    private void registerCommands() {
        Bukkit.getServer().getPluginCommand("draft").setExecutor(new DraftCommand(draftService, threadService, worldService, guiService, permissionService, isUILocked));
        Bukkit.getServer().getPluginCommand("theme").setExecutor(new ThemeCommand(themeService ,draftService, threadService, dataProvider, worldService, guiService, isUILocked));

    }

    @Override
    public void onDisable() {
        running = false;
    }
}
