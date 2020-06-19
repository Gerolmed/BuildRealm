package net.endrealm.lostsouls.config;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class FileUtils {
    public static InputStream getResource(JavaPlugin plugin, String resourcePath) {

        if(resourcePath == null || resourcePath.isEmpty())
            return null;

        resourcePath = resourcePath.replace('\\', '/');

        try {
            URL url = plugin.getClass().getClassLoader().getResource(resourcePath);
            if (url == null) {
                return null;
            } else {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                return connection.getInputStream();
            }
        } catch (IOException var4) {
            return null;
        }
    }
}

