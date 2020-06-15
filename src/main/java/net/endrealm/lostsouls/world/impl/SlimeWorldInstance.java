package net.endrealm.lostsouls.world.impl;

import com.grinderwolf.swm.api.world.SlimeWorld;
import lombok.Data;
import net.endrealm.lostsouls.world.WorldIdentity;
import net.endrealm.lostsouls.world.WorldInstance;
import org.bukkit.World;

import java.util.Optional;

@Data
public class SlimeWorldInstance implements WorldInstance<SlimeWorld> {
    private final WorldIdentity identity;
    private World bukkitWorld;
    private SlimeWorld storageWorld;

    public Optional<World> getBukkitWorld() {
        return Optional.ofNullable(bukkitWorld);
    }

    public Optional<SlimeWorld> getStorageWorld() {
        return Optional.ofNullable(storageWorld);
    }
}
