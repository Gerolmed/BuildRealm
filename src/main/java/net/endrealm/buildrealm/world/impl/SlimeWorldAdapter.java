package net.endrealm.buildrealm.world.impl;

import com.infernalsuite.aswm.api.SlimePlugin;
import com.infernalsuite.aswm.api.exceptions.*;
import com.infernalsuite.aswm.api.loaders.SlimeLoader;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import com.infernalsuite.aswm.api.world.properties.SlimeProperties;
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap;
import lombok.Data;
import net.endrealm.buildrealm.world.WorldAdapter;
import net.endrealm.buildrealm.world.WorldIdentity;
import net.endrealm.buildrealm.world.WorldInstance;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;
import java.util.Objects;

@Data
public class SlimeWorldAdapter implements WorldAdapter<SlimeWorld> {

    private final SlimePlugin slimePlugin;
    private final SlimeLoader openWorldLoader;
    private final SlimeLoader closedWorldLoader;

    public SlimeWorldAdapter(SlimePlugin slimePlugin, SlimeLoader openWorldLoader, SlimeLoader closedWorldLoader) {
        this.slimePlugin = slimePlugin;
        this.openWorldLoader = openWorldLoader;
        this.closedWorldLoader = closedWorldLoader;
    }

    @Override
    public synchronized WorldInstance<SlimeWorld> clone(WorldIdentity original, WorldIdentity target) {

        try {
            SlimeWorld slimeWorld = null;
            if (original.getWorldName().equals(target.getWorldName())) {
                slimePlugin.migrateWorld(target.getWorldName(), getLoader(original), getLoader(target));
                slimeWorld = slimePlugin.loadWorld(getLoader(target), target.getWorldName(), target.isOpen(), getDefaultProperties());
            } else {
                WorldInstance<SlimeWorld> instance = load(original);
                if (instance.getStorageWorld().isPresent())
                    slimeWorld = instance.getStorageWorld().get().clone(target.getWorldName(), getLoader(target));
            }
            SlimeWorldInstance slimeWorldInstance = new SlimeWorldInstance(target);
            slimeWorldInstance.setStorageWorld(slimeWorld);
            return slimeWorldInstance;
        } catch (WorldAlreadyExistsException e) {
            e.printStackTrace();
            return load(target);
        } catch (IOException | UnknownWorldException | WorldLockedException | NewerFormatException | CorruptedWorldException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public synchronized WorldInstance<SlimeWorld> load(WorldIdentity identity) {
        try {
            SlimeWorld slimeWorld = slimePlugin.loadWorld(getLoader(identity), identity.getWorldName(), !identity.isOpen(), getDefaultProperties());
            SlimeWorldInstance slimeWorldInstance = new SlimeWorldInstance(identity);
            slimeWorldInstance.setStorageWorld(slimeWorld);
            return slimeWorldInstance;
        } catch (UnknownWorldException | IOException | CorruptedWorldException | NewerFormatException | WorldLockedException e) {
            e.printStackTrace();
        }
        return new SlimeWorldInstance(identity);
    }

    @Override
    public boolean unload(WorldIdentity identity) {
        SlimeWorldInstance instance = new SlimeWorldInstance(identity);
        instance.setBukkitWorld(Bukkit.getWorld(identity.getWorldName()));
        return unload(instance);
    }

    @Override
    public boolean unload(WorldInstance<SlimeWorld> instance) {
        WorldIdentity identity = instance.getIdentity();
        instance.getBukkitWorld().ifPresent(world -> world.getPlayers().forEach(player -> player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation())));
        return Bukkit.unloadWorld(identity.getWorldName(), true);
    }

    @Override
    public WorldInstance<SlimeWorld> generate(WorldInstance<SlimeWorld> instance) {
        slimePlugin.loadWorld(instance.getStorageWorld().get());
        World world = Bukkit.getWorld(instance.getIdentity().getWorldName());
        SlimeWorldInstance newInstance = new SlimeWorldInstance(instance.getIdentity());
        newInstance.setStorageWorld(instance.getStorageWorld().get());
        newInstance.setBukkitWorld(world);
        return newInstance;
    }

    @Override
    public synchronized void delete(WorldIdentity identity) {
        try {
            SlimeLoader loader = getLoader(identity);
            if (loader.worldExists(identity.getWorldName()))
                loader.unlockWorld(identity.getWorldName());
            loader.deleteWorld(identity.getWorldName());
        } catch (UnknownWorldException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized boolean exists(WorldIdentity identity) {
        try {
            return getLoader(identity).worldExists(identity.getWorldName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public synchronized WorldInstance<SlimeWorld> createEmpty(WorldIdentity worldIdentity) {
        try {
            SlimeLoader loader = getLoader(worldIdentity);
            Objects.requireNonNull(loader);
            SlimeWorld slimeWorld = slimePlugin
                    .createEmptyWorld(
                            loader,
                            worldIdentity.getWorldName(),
                            !worldIdentity.isOpen(),
                            getDefaultProperties()
                    );
            SlimeWorldInstance slimeWorldInstance = new SlimeWorldInstance(worldIdentity);
            slimeWorldInstance.setStorageWorld(slimeWorld);
            return slimeWorldInstance;
        } catch (WorldAlreadyExistsException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void save(WorldInstance<SlimeWorld> worldInstance) {
        worldInstance.getBukkitWorld().ifPresent(World::save);
    }

    @Override
    public boolean unloadHard(WorldIdentity identity) {
        SlimeWorldInstance instance = new SlimeWorldInstance(identity);
        instance.setBukkitWorld(Bukkit.getWorld(identity.getWorldName()));
        return unloadHard(instance);
    }

    @Override
    public boolean unloadHard(WorldInstance<SlimeWorld> instance) {
        WorldIdentity identity = instance.getIdentity();
        instance.getBukkitWorld().ifPresent(world -> world.getPlayers().forEach(player -> player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation())));
        return Bukkit.unloadWorld(identity.getWorldName(), true);
    }

    private SlimeLoader getLoader(WorldIdentity identity) {
        return identity.isOpen() ? openWorldLoader : closedWorldLoader;
    }

    private SlimePropertyMap getDefaultProperties() {
        SlimePropertyMap propertyMap = new SlimePropertyMap();
        propertyMap.setInt(SlimeProperties.SPAWN_X, 0);
        propertyMap.setInt(SlimeProperties.SPAWN_Y, 100);
        propertyMap.setInt(SlimeProperties.SPAWN_Z, 0);
        propertyMap.setBoolean(SlimeProperties.ALLOW_ANIMALS, false);
        propertyMap.setBoolean(SlimeProperties.ALLOW_MONSTERS, false);
        propertyMap.setBoolean(SlimeProperties.PVP, false);

        return propertyMap;
    }
}
