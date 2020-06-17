package net.endrealm.lostsouls.listener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.utils.BaseListener;
import net.endrealm.lostsouls.world.WorldIdentity;
import net.endrealm.lostsouls.world.WorldService;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@EqualsAndHashCode(callSuper = true)
@Data
public class LeaveListener extends BaseListener {
    private final DraftService draftService;
    private final WorldService worldService;

    @EventHandler(priority = EventPriority.LOW)
    public void kicked(PlayerKickEvent worldEvent) {
        leave(worldEvent.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void leaveServer(PlayerQuitEvent worldEvent) {
        leave(worldEvent.getPlayer());
    }

    private void leave(Player player) {
        if(player == null) return;
        World world = player.getWorld();

        String name = world.getName();
        WorldIdentity identity = new WorldIdentity(world.getName(), false);
        // check if its our world
        if(!worldService.isLoaded(identity)) return;

        if(world.getPlayers().size() > 0) {
            return;
        }
        draftService.unloadDraft(name, () -> { }, System.out::println);
    }
}
