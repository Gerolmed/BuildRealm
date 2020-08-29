package net.endrealm.buildrealm.gui;

import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import lombok.RequiredArgsConstructor;
import net.endrealm.buildrealm.chatinput.ChatInputManager;
import net.endrealm.buildrealm.data.PieceType;
import net.endrealm.buildrealm.data.entity.Draft;
import net.endrealm.buildrealm.data.entity.Group;
import net.endrealm.buildrealm.data.entity.Piece;
import net.endrealm.buildrealm.data.entity.TypeCategory;
import net.endrealm.buildrealm.repository.DataProvider;
import net.endrealm.buildrealm.services.DraftService;
import net.endrealm.buildrealm.services.GroupService;
import net.endrealm.buildrealm.services.PermissionService;
import net.endrealm.buildrealm.services.ThreadService;
import net.endrealm.buildrealm.utils.Observable;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class GuiService {
    private final InventoryManager inventoryManager;
    private final DraftService draftService;
    private final ThreadService threadService;
    private final GroupService GroupService;
    private final DataProvider dataProvider;
    private final ChatInputManager chatInputManager;
    private final PermissionService permissionService;
    private final Observable<Boolean> isLocked;

    public SmartInventory getDraftDetails(Draft draft) {
        if (isLocked.get().orElse(false)) return null;

        DraftDetails draftDetails = new DraftDetails(draft, draftService, threadService, GroupService, this);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(draftDetails)
                .size(3, 9)
                .title("Draft@" + draft.getId())
                .build();
        draftDetails.setSmartInventory(smartInventory);
        return smartInventory;
    }

    public SmartInventory getDraftsList(OfflinePlayer player, List<Draft> drafts) {
        if (isLocked.get().orElse(false)) return null;

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
        if (isLocked.get().orElse(false)) return null;

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

    public SmartInventory getGroupsList(List<Group> Groups) {
        if (isLocked.get().orElse(false)) return null;

        GroupList list = new GroupList(Groups, draftService, GroupService, this);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(list)
                .size(4, 9)
                .title("Groups@all")
                .build();
        list.setSmartInventory(smartInventory);

        return smartInventory;
    }

    public SmartInventory getGroupsSelection(List<Group> Groups, Consumer<Group> onSelect, Runnable onCancel) {
        if (isLocked.get().orElse(false)) return null;

        GroupSelection list = new GroupSelection(Groups, draftService, GroupService, this, onSelect, onCancel);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(list)
                .size(4, 9)
                .title("Groups@all#select")
                .build();
        list.setSmartInventory(smartInventory);

        return smartInventory;
    }

    public SmartInventory getTypeSelection(Consumer<PieceType> onSelect, Runnable onCancel) {
        if (isLocked.get().orElse(false)) return null;

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

    public SmartInventory getGroupDetails(Group Group) {
        if (isLocked.get().orElse(false)) return null;

        GroupDetails GroupDetails = new GroupDetails(draftService, GroupService, this, threadService, Group);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(GroupDetails)
                .size(4, 9)
                .title("Group@" + Group.getName())
                .build();
        GroupDetails.setSmartInventory(smartInventory);

        return smartInventory;
    }

    public SmartInventory getEditDraftMembers(Draft draft, Runnable onBack, boolean editable) {
        if (isLocked.get().orElse(false)) return null;

        EditMembers editMembers = new EditMembers(draft, draftService, threadService, this, onBack, editable, chatInputManager);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(editMembers)
                .size(4, 9)
                .title("Members Draft@" + draft.getId())
                .build();
        editMembers.setSmartInventory(smartInventory);
        return smartInventory;
    }

    public SmartInventory getPublishDraft(Draft draft, Runnable onBack) {
        if (isLocked.get().orElse(false)) return null;

        Publish publish = new Publish(draft, onBack, draftService, GroupService, threadService, dataProvider, this);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(publish)
                .size(4, 9)
                .title("Publish Draft@" + draft.getId())
                .build();
        publish.setSmartInventory(smartInventory);
        return smartInventory;
    }

    public SmartInventory getWait() {
        if (isLocked.get().orElse(false)) return null;

        return SmartInventory.builder()
                .manager(inventoryManager)
                .provider(new Wait())
                .size(4, 9)
                .title("Processing....")
                .build();
    }

    public SmartInventory getCategoryDetails(Group Group, TypeCategory category, List<Piece> pieces) {
        if (isLocked.get().orElse(false)) return null;

        CategoryDetails categoryDetails = new CategoryDetails(Group, category, pieces, draftService, threadService, dataProvider, this);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(categoryDetails)
                .size(4, 9)
                .title(Group.getName() + "@" + category.getType().name().toLowerCase())
                .build();
        categoryDetails.setSmartInventory(smartInventory);
        return smartInventory;
    }

    public SmartInventory getPieceDetails(Piece piece) {
        if (isLocked.get().orElse(false)) return null;

        PieceDetails pieceDetails = new PieceDetails(piece, draftService, threadService, GroupService, this, permissionService);
        SmartInventory smartInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .provider(pieceDetails)
                .size(3, 9)
                .title("Piece@" + piece.getId() + "#" + piece.getEffectiveDisplayName(dataProvider))
                .build();
        pieceDetails.setSmartInventory(smartInventory);
        return smartInventory;
    }
}
