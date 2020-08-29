package net.endrealm.buildrealm.listener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.endrealm.buildrealm.services.DraftService;
import net.endrealm.buildrealm.utils.BaseListener;
import net.endrealm.buildrealm.world.WorldIdentity;
import net.endrealm.buildrealm.world.WorldService;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;

@EqualsAndHashCode(callSuper = true)
@Data
public class WorldChangeListener extends BaseListener {
    private final DraftService draftService;
    private final WorldService worldService;

    @EventHandler(priority = EventPriority.LOW)
    public void onChangeFrom(PlayerChangedWorldEvent worldEvent) {
        World world = worldEvent.getFrom();
        Player player = worldEvent.getPlayer();
        String name = world.getName();
        WorldIdentity identity = new WorldIdentity(world.getName(), false);
        // check if its our world
        if (!worldService.isLoaded(identity)) return;

        sendInfo(player, "You left the Draft@" + name);
        if (world.getPlayers().size() > 0) {
            return;
        }
        draftService.unloadDraft(name, () -> sendInfo(player, "Unloaded Draft@" + name), System.out::println);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChangeTo(PlayerChangedWorldEvent worldEvent) {
        Player player = worldEvent.getPlayer();

        World world = player.getWorld();
        String name = world.getName();
        WorldIdentity identity = new WorldIdentity(world.getName(), false);
        // check if its our world
        if (!worldService.isLoaded(identity)) return;

        sendInfo(player, "You entered the Draft@" + name);
    }
}
