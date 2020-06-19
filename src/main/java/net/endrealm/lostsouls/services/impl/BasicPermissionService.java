package net.endrealm.lostsouls.services.impl;

import lombok.Data;
import net.endrealm.lostsouls.config.MainConfig;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.PermissionService;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.function.Consumer;

@Data
public class BasicPermissionService implements PermissionService {

    private final MainConfig mainConfig;
    private final DraftService draftService;

    @Override
    public int maxDrafts(Player player) {

        if(player.hasPermission("souls_save.draft.max.infinite")) return -1;

        for(int step : mainConfig.getMaxDraftSteps()) {
            if(player.hasPermission("souls_save.draft.max."+step))
                return step;
        }
        return 0;
    }

    @Override
    public void currentDrafts(Player player, Consumer<Integer> onLoad) {
        draftService.accessibleDrafts(player.getUniqueId(), drafts -> {
            int amount = (int) drafts.stream().filter(draft -> draft.hasOwner(player.getUniqueId())).count();
            onLoad.accept(amount);
        });
    }
}
