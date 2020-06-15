package net.endrealm.lostsouls;

import com.grinderwolf.swm.api.SlimePlugin;
import lombok.Getter;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.repository.impl.BasicCache;
import net.endrealm.lostsouls.repository.impl.BasicDataProvider;
import net.endrealm.lostsouls.repository.impl.BasicDraftRepository;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.services.impl.ThreadServiceImpl;
import net.endrealm.realmdrive.factory.DriveServiceFactory;
import net.endrealm.realmdrive.utils.DriveSettings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class LostSoulsSave extends JavaPlugin {

    private DataProvider dataProvider;
    private ThreadService threadService;
    private final Long CACHE_DURATION = 40000L;
    private SlimePlugin slimePlugin;

    @Override
    public void onEnable() {
        slimePlugin = (SlimePlugin) Bukkit.getServer().getPluginManager().getPlugin("SlimeWorldManager");
        threadService = new ThreadServiceImpl(this);

        // Plugin startup logic
        DriveSettings settings = DriveSettings.builder()
                .type(DriveSettings.BackendType.MONGO_DB)
                .hostURL("mongodb://localhost:27017")
                .database("soulssave")
                .table("primary")
                .build();

        DriveServiceFactory serviceFactory = new DriveServiceFactory();

        this.dataProvider = new BasicDataProvider(
                new BasicCache<>(CACHE_DURATION),
                new BasicDraftRepository(serviceFactory.getDriveService(settings))
        );
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
