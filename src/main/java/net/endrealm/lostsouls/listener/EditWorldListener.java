package net.endrealm.lostsouls.listener;

import com.sk89q.worldedit.event.platform.BlockInteractEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.utils.BaseListener;
import net.endrealm.lostsouls.world.WorldIdentity;
import net.endrealm.lostsouls.world.WorldService;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@EqualsAndHashCode(callSuper = true)
@Data
public class EditWorldListener extends BaseListener {
    private final DataProvider dataProvider;
    private final WorldService worldService;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreakBlock(BlockBreakEvent event) {
        event.setCancelled(!isPermitted(event.getPlayer(), event.getPlayer().getWorld()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlaceBLock(BlockPlaceEvent event) {
        event.setCancelled(!isPermitted(event.getPlayer(), event.getPlayer().getWorld()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractEvent(PlayerInteractEvent event) {
        event.setCancelled(!isPermitted(event.getPlayer(), event.getPlayer().getWorld()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(!isPermitted(event.getPlayer(), event.getPlayer().getWorld()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickup(EntityPickupItemEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        event.setCancelled(!isPermitted((Player)event.getEntity(), event.getEntity().getWorld()));
    }

    private boolean isPermitted(Player player, World world) {
        if(player == null) return true;
        if(!worldService.isLoaded(new WorldIdentity(world.getName(), false))) return true;
        return player.hasPermission("souls_save.draft.edit.other") || dataProvider.getDraft(world.getName()).map(draft -> draft.hasMember(player.getUniqueId())).orElse(false);
    }
}
