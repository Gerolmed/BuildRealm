package net.endrealm.buildrealm.world;

import org.bukkit.World;

import java.util.Optional;

public interface WorldInstance<T> {
    WorldIdentity getIdentity();

    Optional<World> getBukkitWorld();

    Optional<T> getStorageWorld();
}
