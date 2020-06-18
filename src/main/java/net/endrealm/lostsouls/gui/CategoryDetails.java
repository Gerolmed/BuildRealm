package net.endrealm.lostsouls.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import lombok.Data;
import lombok.Setter;
import net.endrealm.lostsouls.Constants;
import net.endrealm.lostsouls.data.entity.Piece;
import net.endrealm.lostsouls.data.entity.Theme;
import net.endrealm.lostsouls.data.entity.TypeCategory;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Data
public class CategoryDetails implements InventoryProvider {
    private final Theme theme;
    private final TypeCategory category;
    private final List<Piece> drafts;
    private final DraftService draftService;
    private final ThreadService threadService;
    private final DataProvider dataProvider;
    private final GuiService guiService;
    @Setter
    private SmartInventory smartInventory;

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)));

        Pagination pagination = contents.pagination();
        pagination.setItemsPerPage(14);
        pagination.setItems(drafts.stream().map(
                draft -> ClickableItem.of(ItemBuilder.builder(Material.GRASS_BLOCK)
                                .displayName("§6Piece@" + draft.getId() +"#"+draft.getEffectiveName(dataProvider))
                                .addLore("§bClick for more actions")
                                .build(),
                        inventoryClickEvent -> {
                            if(draft.isInvalid()) {
                                player.sendMessage(Constants.ERROR_PREFIX+Constants.DRAFT_INVALIDATED);
                                player.closeInventory();
                                return;
                            }
                            guiService.getPieceDetails(draft).open(player);
                        })
        ).toArray(ClickableItem[]::new));

        ClickableItem[] items = pagination.getPageItems();
        int half = items.length / 2;
        for (int i = 0; i < half; i++) {
            contents.set(1, i+1, items[i]);
            contents.set(2, i+1, items[i+half]);
        }

        if(!pagination.isFirst())
            contents.set(3, 3, ClickableItem.of(ItemBuilder.builder(Material.ARROW).displayName("§6Previous Page").build(),
                e -> smartInventory.open(player, pagination.previous().getPage())));
        if(!pagination.isLast())
            contents.set(3, 5, ClickableItem.of(ItemBuilder.builder(Material.ARROW).displayName("§6Next Page").build(),
                e -> smartInventory.open(player, pagination.next().getPage())));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
