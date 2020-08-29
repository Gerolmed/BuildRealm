package net.endrealm.buildrealm.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import lombok.Data;
import lombok.Setter;
import net.endrealm.buildrealm.Constants;
import net.endrealm.buildrealm.data.entity.Group;
import net.endrealm.buildrealm.services.DraftService;
import net.endrealm.buildrealm.services.GroupService;
import net.endrealm.buildrealm.utils.NameComparator;
import net.endrealm.buildrealm.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Data
public class GroupList implements InventoryProvider {
    private final List<Group> drafts;
    private final DraftService draftService;
    private final GroupService GroupService;
    private final GuiService guiService;
    @Setter
    private SmartInventory smartInventory;

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)));

        boolean seeAllPerm = player.hasPermission("souls_save.Group.see.all");
        boolean useAll = player.hasPermission("souls_save.Group.open.all");
        Pagination pagination = contents.pagination();
        pagination.setItemsPerPage(14);
        pagination.setItems(drafts.stream().filter(Group -> seeAllPerm || useAll || player.hasPermission("souls_save.Group.open." + Group.getName())).map(
                Group -> ClickableItem.of(
                        ItemBuilder
                                .builder(Material.CHEST)
                                .displayName("§6" + Group.getName())
                                .addLore("§bIsComplete: " + (Group.isComplete() ? "§ayes" : "§cno"))
                                .addLore("§bStale: " + (Group.isStale() ? "§ayes" : "§cno"))
                                .build(),
                        inventoryClickEvent -> {
                            if (Group.isInvalid()) {
                                player.sendMessage(Constants.ERROR_PREFIX + Constants.Group_INVALIDATED);
                                player.closeInventory();
                                return;
                            }
                            if (!useAll && !player.hasPermission("souls_save.Group.open." + Group.getName())) {
                                player.sendMessage(Constants.ERROR_PREFIX + Constants.NO_PERMISSION);
                                return;
                            }

                            guiService.getGroupDetails(Group).open(player);
                        })
        ).sorted(new NameComparator<>(type -> type.getItem().getItemMeta().getDisplayName())).toArray(ClickableItem[]::new));

        ClickableItem[] items = pagination.getPageItems();
        int half = items.length / 2;
        for (int i = 0; i < half; i++) {
            contents.set(1, i + 1, items[i]);
            contents.set(2, i + 1, items[i + half]);
        }

        if (!pagination.isFirst())
            contents.set(3, 3, ClickableItem.of(ItemBuilder.builder(Material.ARROW).displayName("§6Previous Page").build(),
                    e -> smartInventory.open(player, pagination.previous().getPage())));
        if (!pagination.isLast())
            contents.set(3, 5, ClickableItem.of(ItemBuilder.builder(Material.ARROW).displayName("§6Next Page").build(),
                    e -> smartInventory.open(player, pagination.next().getPage())));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
