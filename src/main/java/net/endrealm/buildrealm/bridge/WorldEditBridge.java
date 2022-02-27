package net.endrealm.buildrealm.bridge;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import net.endrealm.buildrealm.repository.DataProvider;
import net.endrealm.buildrealm.world.WorldService;
import org.bukkit.Bukkit;

public class WorldEditBridge {
    public static void setup(DataProvider dataProvider, WorldService worldService) {
        var plugin = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        plugin.getWorldEdit().getEventBus().register(new WorldEditListener(dataProvider, worldService));
    }
}
