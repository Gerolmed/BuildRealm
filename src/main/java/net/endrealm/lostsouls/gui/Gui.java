package net.endrealm.lostsouls.gui;

import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import net.endrealm.lostsouls.data.entity.Draft;
import org.bukkit.entity.Player;

public final class Gui {

    private static InventoryManager inventoryManager;

    public Gui(InventoryManager inventoryManager) {
        Gui.inventoryManager = inventoryManager;
    }

    public static SmartInventory getDraftDetails(Player player, Draft draft) {
        return SmartInventory.builder()
                .manager(inventoryManager)
                .provider(new DraftDetails(player, draft))
                .size(3, 9)
                .title("Draft@" + draft.getId())
                .build();
    }
}
