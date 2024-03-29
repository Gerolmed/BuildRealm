package net.endrealm.buildrealm.bridge;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import lombok.RequiredArgsConstructor;
import net.endrealm.buildrealm.repository.DataProvider;
import net.endrealm.buildrealm.world.WorldIdentity;
import net.endrealm.buildrealm.world.WorldService;

@RequiredArgsConstructor
public class WorldEditListener {
    private final DataProvider dataProvider;
    private final WorldService worldService;

    @Subscribe(priority = EventHandler.Priority.VERY_EARLY)
    public void onEditSessionEvent(EditSessionEvent event) {
        Actor actor = event.getActor();
        if (actor != null && actor.isPlayer()) {
            if (actor.hasPermission("build_realm.draft.edit.other")) return;
            World world = event.getWorld();
            String name = world.getName();
            WorldIdentity identity = new WorldIdentity(name, false);
            // check if its our world
            if (!worldService.isLoaded(identity)) return;
            event.setExtent(new AntiWorldEdit(world, event.getExtent(), (Player) actor, dataProvider.getDraft(name).get()));
        }
    }
}
