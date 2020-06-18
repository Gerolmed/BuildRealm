package net.endrealm.lostsouls.gui;

import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import lombok.RequiredArgsConstructor;
import net.endrealm.lostsouls.chatinput.ChatInputManager;
import net.endrealm.lostsouls.data.PieceType;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.data.entity.Piece;
import net.endrealm.lostsouls.data.entity.Theme;
import net.endrealm.lostsouls.data.entity.TypeCategory;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThemeService;
import net.endrealm.lostsouls.services.ThreadService;
import org.bukkit.OfflinePlayer;

import java.lang.module.ModuleReader;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class GuiService {
    private final InventoryManager inventoryManager;
    private final DraftService draftService;
    private final ThreadService threadService;
    private final ThemeService themeService;
    private final DataProvider dataProvider;
    private final ChatInputManager chatInputManager;

    public SmartInventory getDraftDetails(Draft draft) {
        DraftDetails draftDetails = new DraftDetails(draft, draftService, threadService, themeService, this);
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

    public SmartInventory getThemesSelection(List<Theme> themes, Consumer<Theme> onSelect, Runnable onCancel) {
        ThemeSelection list = new ThemeSelection(themes, draftService, themeService, this, onSelect, onCancel);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(list)
                .size(4, 9)
                .title("Themes@all#select")
                .build();
        list.setSmartInventory(smartInventory);

        return smartInventory;
    }

    public SmartInventory getTypeSelection(Consumer<PieceType> onSelect, Runnable onCancel) {
        TypeSelection typeSelection = new TypeSelection(onSelect, onCancel);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(typeSelection)
                .size(4, 9)
                .title("Types#select")
                .build();
        typeSelection.setSmartInventory(smartInventory);

        return smartInventory;
    }

    public SmartInventory getThemeDetails(Theme theme) {
        ThemeDetails themeDetails = new ThemeDetails(draftService, themeService, this, threadService, theme);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(themeDetails)
                .size(4, 9)
                .title("Theme@"+theme.getName())
                .build();
        themeDetails.setSmartInventory(smartInventory);

        return smartInventory;
    }

    public SmartInventory getEditDraftMembers(Draft draft, Runnable onBack, boolean editable) {
        EditMembers editMembers = new EditMembers(draft, draftService, threadService, this, onBack, editable, chatInputManager);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(editMembers)
                .size(4, 9)
                .title("Members Draft@"+draft.getId())
                .build();
        editMembers.setSmartInventory(smartInventory);
        return smartInventory;
    }

    public SmartInventory getPublishDraft(Draft draft, Runnable onBack) {
        Publish publish = new Publish(draft, onBack, draftService, themeService, threadService, dataProvider, this);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(publish)
                .size(4, 9)
                .title("Publish Draft@"+draft.getId())
                .build();
        publish.setSmartInventory(smartInventory);
        return smartInventory;
    }

    public SmartInventory getWait() {
        return SmartInventory.builder()
                .manager(inventoryManager)
                .provider(new Wait())
                .size(4, 9)
                .title("Processing....")
                .build();
    }

    public SmartInventory getCategoryDetails(Theme theme, TypeCategory category, List<Piece> pieces) {
        CategoryDetails categoryDetails = new CategoryDetails(theme, category, pieces, draftService, threadService, dataProvider, this);
        SmartInventory smartInventory =  SmartInventory.builder()
                .manager(inventoryManager)
                .provider(categoryDetails)
                .size(4, 9)
                .title(theme.getName() + "@" + category.getType().name().toLowerCase())
                .build();
        categoryDetails.setSmartInventory(smartInventory);
        return smartInventory;
    }

    public SmartInventory getPieceDetails(Piece piece) {
        PieceDetails pieceDetails = new PieceDetails(piece, draftService, threadService, themeService, this);
        SmartInventory smartInventory =  SmartInventory.builder()
                .manager(inventoryManager)
                .provider(pieceDetails)
                .size(3, 9)
                .title("Piece@" + piece.getId())
                .build();
        pieceDetails.setSmartInventory(smartInventory);
        return smartInventory;
    }
}
