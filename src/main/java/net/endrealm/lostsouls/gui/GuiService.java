package net.endrealm.lostsouls.gui;

import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import lombok.RequiredArgsConstructor;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.data.entity.Theme;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThemeService;
import net.endrealm.lostsouls.services.ThreadService;
import org.bukkit.OfflinePlayer;

import java.util.List;

@RequiredArgsConstructor
public class GuiService {
    private final InventoryManager inventoryManager;
    private final DraftService draftService;
    private final ThreadService threadService;
    private final ThemeService themeService;

    public SmartInventory getDraftDetails(Draft draft) {
        DraftDetails draftDetails = new DraftDetails(draft, draftService, threadService, this);
        SmartInventory smartInventory =  SmartInventory.builder()
                .manager(inventoryManager)
                .provider(draftDetails)
                .size(3, 9)
                .title("Draft@" + draft.getId())
                .build();
        draftDetails.setSmartInventory(smartInventory);
        return smartInventory;
    }
    public SmartInventory getDraftsList(OfflinePlayer player, List<Draft> drafts) {
        DraftList list = new DraftList(drafts, draftService, threadService, this);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(list)
                .size(4, 9)
                .title("Drafts@" + player.getName())
                .build();
        list.setSmartInventory(smartInventory);

        return smartInventory;
    }

    public SmartInventory getConfirmationWindow(String title, Runnable onConfirm, Runnable onCancel) {
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

    public SmartInventory getThemesList(List<Theme> themes) {
        ThemeList list = new ThemeList(themes, draftService, themeService, this);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(list)
                .size(4, 9)
                .title("Themes@all")
                .build();
        list.setSmartInventory(smartInventory);

        return smartInventory;
    }

    public SmartInventory getThemeDetails(Theme theme) {
        ThemeDetails themeDetails = new ThemeDetails(draftService, themeService, this, theme);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(themeDetails)
                .size(4, 9)
                .title("Theme@"+theme.getName())
                .build();
        themeDetails.setSmartInventory(smartInventory);

        return smartInventory;
    }

    public SmartInventory getEditDraftMembers(Draft draft, Runnable onBack) {
        EditMembers editMembers = new EditMembers(draft, draftService, threadService, this, onBack);
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
