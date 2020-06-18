package net.endrealm.lostsouls.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import lombok.Data;
import lombok.Setter;
import net.endrealm.lostsouls.Constants;
import net.endrealm.lostsouls.data.PieceType;
import net.endrealm.lostsouls.data.entity.Theme;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThemeService;
import net.endrealm.lostsouls.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Data
public class TypeSelection implements InventoryProvider {
    private final Consumer<PieceType> onSelect;
    private final Runnable onCancel;

    @Setter
    private SmartInventory smartInventory;

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)));
        Pagination pagination = contents.pagination();
        pagination.setItemsPerPage(14);
        pagination.setItems(Arrays.stream(PieceType.values()).map(
                type -> ClickableItem.of(
                        ItemBuilder
                                .builder(Material.CHEST)
                                .displayName("§6" + type.getDisplayName())
                                .addLore("§a§lClick to select")
                                .build(),
                        inventoryClickEvent -> {
                            onSelect.accept(type);
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
        contents.set(3, 4, ClickableItem.of(
                ItemBuilder.builder(Material.BARRIER).displayName("§cBack").build(),
                inventoryClickEvent -> {
                    onCancel.run();
                }
        ));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
