package net.endrealm.buildrealm.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import lombok.Data;
import net.endrealm.buildrealm.Constants;
import net.endrealm.buildrealm.data.PieceType;
import net.endrealm.buildrealm.data.entity.Group;
import net.endrealm.buildrealm.data.entity.Piece;
import net.endrealm.buildrealm.data.entity.TypeCategory;
import net.endrealm.buildrealm.services.DraftService;
import net.endrealm.buildrealm.services.GroupService;
import net.endrealm.buildrealm.services.ThreadService;
import net.endrealm.buildrealm.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class GroupDetails implements InventoryProvider {
    private final DraftService draftService;
    private final GroupService GroupService;
    private final GuiService guiService;
    private final ThreadService threadService;
    private final Group Group;
    private SmartInventory smartInventory;

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(ItemBuilder.builder(Material.GREEN_STAINED_GLASS_PANE).build()));

        // Can edit Group
        if (player.hasPermission("build_realm.Group.edit.all") || player.hasPermission("build_realm.Group.edit." + Group.getName())) {
            contents.set(0, 2, ClickableItem.of(ItemBuilder.builder(Material.REPEATER).displayName("§aSettings").build(),
                    inventoryClickEvent -> {
                        player.sendMessage("todo: open settings");
                    })
            );
        }

        contents.set(0, 4, ClickableItem.empty(
                ItemBuilder
                        .builder(Material.BOOK)
                        .displayName("§6Group@§e" + Group.getName())
                        .addLore("§bIsComplete: " + (Group.isComplete() ? "§ayes" : "§cno"))
                        .addLore("§bStale: " + (Group.isStale() ? "§ayes" : "§cno"))
                        .build()));

        // Can delete Group
        if (player.hasPermission("build_realm.Group.delete.all") || player.hasPermission("build_realm.Group.delete." + Group.getName())) {
            contents.set(0, 6, ClickableItem.of(ItemBuilder.builder(Material.BARRIER).displayName("§cDelete").build(),
                    inventoryClickEvent -> {
                        guiService.getConfirmationWindow("Delete Group " + Group.getName(),
                                () -> {
                                    GroupService.deleteGroup(Group, () -> player.sendMessage(Constants.PREFIX + "Group " + Group.getName() + " was deleted! Its drafts are now floating (/draft floating)"));
                                },
                                () -> smartInventory.open(player)).open(player);
                    })
            );
        }
        List<TypeCategory> categories = new ArrayList<>(Group.getCategoryList());
        categories.sort(Comparator.comparingInt(o -> o.getType().ordinal()));
        for (int i = 0; i < categories.size(); i++) {
            int col = i < 7 ? i : i - 7;
            int row = i < 7 ? 1 : 2;
            TypeCategory category = categories.get(i);
            PieceType type = category.getType();
            contents.set(row, col + 1, ClickableItem.of(
                    ItemBuilder.builder(type.getMaterial()).displayName("§6" + type.getDisplayName()).addLore("§aCount: §7" + category.getPieceCount()).build(),
                    inventoryClickEvent -> draftService.draftsByGroupAndType(Group, category.getType(), drafts -> threadService.runSync(() -> guiService.getCategoryDetails(Group, category, drafts.stream().map(draft -> (Piece) draft).collect(Collectors.toList())).open(player)))
            ));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
