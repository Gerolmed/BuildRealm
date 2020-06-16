package net.endrealm.lostsouls;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import fr.minuskube.inv.InventoryManager;
import lombok.Getter;
import net.endrealm.lostsouls.commands.DraftCommand;
import net.endrealm.lostsouls.gui.Gui;
import net.endrealm.lostsouls.listener.WorldChangeListener;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.repository.impl.BasicCache;
import net.endrealm.lostsouls.repository.impl.BasicDataProvider;
import net.endrealm.lostsouls.repository.impl.BasicDraftRepository;
import net.endrealm.lostsouls.repository.impl.BasicThemeRepository;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.services.impl.BasicDraftService;
import net.endrealm.lostsouls.services.impl.ThreadServiceImpl;
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

@Getter
public final class LostSoulsSave extends JavaPlugin {

    private DataProvider dataProvider;
    private ThreadService threadService;
    private WorldService worldService;
    private DraftService draftService;
    private final Long CACHE_DURATION = 40000L;
    private SlimePlugin slimePlugin;
    private InventoryManager inventoryManager;

    @Override
    public void onEnable() {

        String version = Bukkit.getServer().getClass().getPackage().getName();
        String nmsVersion = version.substring(version.lastIndexOf('.') + 1);

        inventoryManager = new InventoryManager(this);
        inventoryManager.init();
        new Gui(inventoryManager);

        slimePlugin = (SlimePlugin) Bukkit.getServer().getPluginManager().getPlugin("SlimeWorldManager");
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

        this.dataProvider = new BasicDataProvider(
                new BasicCache<>(CACHE_DURATION),
                new BasicDraftRepository(driveService),
                new BasicCache<>(CACHE_DURATION),
                new BasicThemeRepository(driveService)
        );
        this.worldService = new BasicWorldService<>(new SlimeWorldAdapter(slimePlugin, getSlimeLoader("openDrafts"), getSlimeLoader("closedDrafts")), threadService);
        this.draftService = new BasicDraftService(dataProvider, threadService, worldService);

        registerCommands();
        registerEvents();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new WorldChangeListener(draftService, worldService), this);
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
        Bukkit.getServer().getPluginCommand("draft").setExecutor(new DraftCommand(draftService, threadService, worldService));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
