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

    public static SmartInventory getDraftDetails(Draft draft, DraftService draftService) {
        DraftDetails draftDetails = new DraftDetails(draft, draftService);
        SmartInventory smartInventory =  SmartInventory.builder()
                .manager(inventoryManager)
                .provider(draftDetails)
                .size(3, 9)
                .title("Draft@" + draft.getId())
                .build();
        draftDetails.setSmartInventory(smartInventory);
        return smartInventory;
    }
    public static SmartInventory getDraftsList(Player player, List<Draft> drafts, DraftService draftService) {
        DraftList list = new DraftList(drafts, draftService);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(list)
                .size(4, 9)
                .title("Drafts@" + player.getName())
                .build();
        list.setSmartInventory(smartInventory);

        return smartInventory;
    }

    public static SmartInventory getConfirmationWindow(String title, Runnable onConfirm, Runnable onCancel) {
        Confirmation confirmation = new Confirmation(onConfirm, onCancel);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(confirmation)
                .size(5, 9)
                .title(title)
                .closeable(false)
                .build();
        confirmation.setSmartInventory(smartInventory);

        return smartInventory;
    }
}
