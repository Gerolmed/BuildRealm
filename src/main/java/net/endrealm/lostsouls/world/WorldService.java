package net.endrealm.lostsouls.world;


import org.bukkit.World;

import java.util.function.Consumer;

public interface WorldService {
    /**
     * Generates a new world, Loads a world or finds a loaded world
     * Will call consumer with the wanted world. This call <b>may be async or synced</b> to Bukkit!
     *
     * @param worldIdentity the identity of the world
     * @param worldConsumer this will get called after the world is loaded. WARNING: It can be sync or async
     */
    void generate(WorldIdentity worldIdentity, Consumer<World> worldConsumer);

    /**
     * Clones a world to a given target
     *
     * @param worldIdentity original
     * @param target target
     */
    void clone(WorldIdentity worldIdentity, WorldIdentity target, Runnable onSuccess);

    /**
     * Unloads a chosen world
     */
    void unload(WorldIdentity worldIdentity, Runnable onSuccess);

    void delete(WorldIdentity worldIdentity, Runnable onSuccess);
    boolean isLoaded(WorldIdentity worldIdentity);

    void replace(WorldIdentity old, WorldIdentity newIdentity, Runnable onSuccess);
}
