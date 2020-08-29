package net.endrealm.buildrealm.services.impl;

import lombok.Data;
import net.endrealm.buildrealm.services.ThreadService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Data
public class ThreadServiceImpl implements ThreadService {

    private final JavaPlugin javaPlugin;

    @Override
    public void runAsync(Runnable runnable) {
        if (!Bukkit.isPrimaryThread()) {
            runnable.run();
            return;
        }
        new Thread(runnable).start();
    }

    @Override
    public void runSync(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
            return;
        }
        Bukkit.getScheduler().runTask(javaPlugin, runnable);
    }
}
