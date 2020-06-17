package net.endrealm.lostsouls.gui;

import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.data.entity.Theme;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThemeService;
import net.endrealm.lostsouls.services.ThreadService;
import org.bukkit.OfflinePlayer;

import java.util.List;

public final class Gui {

    private static InventoryManager inventoryManager;

    public Gui(InventoryManager inventoryManager) {
        Gui.inventoryManager = inventoryManager;
    }

    public static SmartInventory getDraftDetails(Draft draft, DraftService draftService, ThreadService threadService) {
        DraftDetails draftDetails = new DraftDetails(draft, draftService, threadService);
        SmartInventory smartInventory =  SmartInventory.builder()
                .manager(inventoryManager)
                .provider(draftDetails)
                .size(3, 9)
                .title("Draft@" + draft.getId())
                .build();
        draftDetails.setSmartInventory(smartInventory);
        return smartInventory;
    }
    public static SmartInventory getDraftsList(OfflinePlayer player, List<Draft> drafts, DraftService draftService, ThreadService threadService) {
        DraftList list = new DraftList(drafts, draftService, threadService);
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

    public static SmartInventory getThemesList(List<Theme> themes, DraftService draftService, ThemeService themeService) {
        ThemeList list = new ThemeList(themes, draftService, themeService);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(list)
                .size(4, 9)
                .title("Themes@all")
                .build();
        list.setSmartInventory(smartInventory);

        return smartInventory;
    }

    public static SmartInventory getThemeDetails(DraftService draftService, ThemeService themeService, Theme theme) {
        ThemeDetails themeDetails = new ThemeDetails(draftService, themeService, theme);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(themeDetails)
                .size(4, 9)
                .title("Theme@"+theme.getName())
                .build();
        themeDetails.setSmartInventory(smartInventory);

        return smartInventory;
    }

    public static SmartInventory getEditDraftMembers(Draft draft, DraftService draftService, ThreadService threadService, Runnable onBack) {
        EditMembers editMembers = new EditMembers(draft, draftService, threadService, onBack);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(editMembers)
                .size(4, 9)
                .title("Members Draft@"+draft.getId())
                .build();
        editMembers.setSmartInventory(smartInventory);
        return smartInventory;
    }
}
