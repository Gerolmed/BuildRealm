package net.endrealm.buildrealm;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import fr.minuskube.inv.InventoryManager;
import lombok.Getter;
import net.endrealm.buildrealm.bridge.WorldEditBridge;
import net.endrealm.buildrealm.chatinput.ChatInputManager;
import net.endrealm.buildrealm.commands.DraftCommand;
import net.endrealm.buildrealm.commands.GroupCommand;
import net.endrealm.buildrealm.config.Configuration;
import net.endrealm.buildrealm.config.MainConfig;
import net.endrealm.buildrealm.gui.GuiService;
import net.endrealm.buildrealm.listener.ChatListener;
import net.endrealm.buildrealm.listener.EditWorldListener;
import net.endrealm.buildrealm.listener.LeaveListener;
import net.endrealm.buildrealm.listener.WorldChangeListener;
import net.endrealm.buildrealm.repository.DataProvider;
import net.endrealm.buildrealm.repository.DraftRepository;
import net.endrealm.buildrealm.repository.GroupRepository;
import net.endrealm.buildrealm.repository.impl.*;
import net.endrealm.buildrealm.services.DraftService;
import net.endrealm.buildrealm.services.GroupService;
import net.endrealm.buildrealm.services.PermissionService;
import net.endrealm.buildrealm.services.ThreadService;
import net.endrealm.buildrealm.services.impl.BasicDraftService;
import net.endrealm.buildrealm.services.impl.BasicGroupService;
import net.endrealm.buildrealm.services.impl.BasicPermissionService;
import net.endrealm.buildrealm.services.impl.ThreadServiceImpl;
import net.endrealm.buildrealm.utils.Observable;
import net.endrealm.buildrealm.world.WorldIdentity;
import net.endrealm.buildrealm.world.WorldService;
import net.endrealm.buildrealm.world.impl.BasicWorldService;
import net.endrealm.buildrealm.world.impl.FileLoader;
import net.endrealm.buildrealm.world.impl.SlimeWorldAdapter;
import net.endrealm.realmdrive.factory.DriveServiceFactory;
import net.endrealm.realmdrive.interfaces.DriveService;
import net.endrealm.realmdrive.utils.DriveSettings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

@Getter
public final class BuildRealm extends JavaPlugin {

    private DataProvider dataProvider;
    private ThreadService threadService;
    private GroupService groupService;
    private WorldService worldService;
    private DraftService draftService;
    private PermissionService permissionService;
    private ChatInputManager chatInputManager;
    private GuiService guiService;
    private final Long CACHE_DURATION = 150000L;
    private SlimePlugin slimePlugin;
    private InventoryManager inventoryManager;
    private MainConfig mainConfig;
    private boolean running;
    private Observable<Boolean> isUILocked;

    @Override
    public void onEnable() {

        isUILocked = Observable.of(false);
        initConfigs();
        inventoryManager = new InventoryManager(this);
        inventoryManager.init();

        slimePlugin = (SlimePlugin) Bukkit.getServer().getPluginManager().getPlugin("SlimeWorldManager");
        registerLoaders();

        threadService = new ThreadServiceImpl(this);


        DraftRepository draftRepository = null;
        GroupRepository groupRepository = null;
        if(mainConfig.getBackend() == MainConfig.BackendType.MONGO) {
            // Plugin startup logic
            var settings = DriveSettings.builder()
                    .type(DriveSettings.BackendType.MONGO_DB)
                    .hostURL(mainConfig.getMongoSettings().host())
                    .database(mainConfig.getMongoSettings().database())
                    .table("primary");
            mainConfig.getMongoSettings().password().ifPresent(settings::password);

            DriveServiceFactory serviceFactory = new DriveServiceFactory();
            DriveService driveService = serviceFactory.getDriveService(settings.build());

            draftRepository = new BasicDraftRepository(driveService);
            groupRepository = new BasicGroupRepository(driveService);
        } else {
            draftRepository = new SQLLiteDraftRepository(new File(getDataFolder(), "local"), getLogger());
            groupRepository = new SQLLiteGroupRepository(new File(getDataFolder(), "local"), getLogger());
        }
        this.chatInputManager = new ChatInputManager();

        //TODO: add cache cleanup on iteration
        this.dataProvider = new BasicDataProvider(
                new BasicCache<>(CACHE_DURATION),
                draftRepository,
                new BasicCache<>(CACHE_DURATION),
                groupRepository
        );
        this.worldService = new BasicWorldService<>(new SlimeWorldAdapter(slimePlugin, getSlimeLoader(mainConfig.getOpenedDraftLoader()), getSlimeLoader(mainConfig.getClosedDraftLoader())), threadService);
        this.groupService = new BasicGroupService(dataProvider, threadService);
        this.draftService = new BasicDraftService(dataProvider, threadService, worldService, groupService);
        this.permissionService = new BasicPermissionService(mainConfig, draftService);

        this.guiService = new GuiService(inventoryManager, draftService, threadService, groupService, dataProvider, chatInputManager, permissionService, isUILocked);
        registerCommands();
        registerEvents();

        if(getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            WorldEditBridge.setup(dataProvider, worldService);
        }


        startWorkers();
    }

    private void startWorkers() {
        threadService.runAsync(() -> {

            if (running) return;

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
            if (!isLocked) return;
            threadService.runSync(() -> Bukkit.getOnlinePlayers().forEach(player -> inventoryManager.getInventory(player).ifPresent(smartInventory -> smartInventory.close(player))));
        });
        // Close all open worlds when ui is locked to prevent corrupt worlds on export
        isUILocked.subscribe(isLocked -> {
            if (!isLocked) return;
            Bukkit.getWorlds().forEach(world -> {
                WorldIdentity identity = new WorldIdentity(world.getName(), false);
                if (!worldService.isLoaded(identity))
                    return;
                worldService.unloadSync(identity);
            });
        });
    }

    private void initConfigs() {
        try {
            this.mainConfig = new MainConfig(new Configuration(this, "config.yml"));
            new Configuration(this, "groupDefaultSettings.yml");
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
        Bukkit.getServer().getPluginCommand("group").setExecutor(new GroupCommand(getDataFolder(), groupService, draftService, threadService, dataProvider, worldService, guiService, isUILocked));

    }

    @Override
    public void onDisable() {
        running = false;
    }
}
