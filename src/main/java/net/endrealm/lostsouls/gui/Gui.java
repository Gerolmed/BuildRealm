package net.endrealm.lostsouls.gui;

import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.services.DraftService;
import org.bukkit.entity.Player;

import java.util.List;

public final class Gui {

    private static InventoryManager inventoryManager;

    public Gui(InventoryManager inventoryManager) {
        Gui.inventoryManager = inventoryManager;
    }

    public static SmartInventory getDraftDetails(Player player, Draft draft, DraftService draftService) {
        return SmartInventory.builder()
                .manager(inventoryManager)
                .provider(new DraftDetails(player, draft, draftService))
                .size(3, 9)
                .title("Draft@" + draft.getId())
                .build();
    }
    public static SmartInventory getDraftsList(Player player, List<Draft> drafts, DraftService draftService) {
        DraftList list = new DraftList(player, drafts, draftService);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(list)
                .size(4, 9)
                .title("Drafts@" + player.getName())
                .build();
        list.setSmartInventory(smartInventory);

        return smartInventory;
    }
}
