package net.endrealm.lostsouls.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.endrealm.lostsouls.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class Confirmation implements InventoryProvider {
    private final Runnable onConfirm;
    private final Runnable onCancel;
    @Setter
    private SmartInventory smartInventory;

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fill(ClickableItem.empty(ItemBuilder.builder(Material.GREEN_STAINED_GLASS_PANE).build()));
        ClickableItem confirmItem = ClickableItem.of(ItemBuilder.builder(Material.EMERALD_BLOCK).displayName("§a§lConfirm").build(),
                inventoryClickEvent -> {
                    smartInventory.close(player);
                    onConfirm.run();
                }
        );
        contents.set(2, 2, confirmItem);

        contents.fillRect(1, 1, 3, 3, confirmItem);

        ClickableItem cancelItem = ClickableItem.of(ItemBuilder.builder(Material.REDSTONE_BLOCK).displayName("§c§lCancel").build(),
                inventoryClickEvent -> {
                    smartInventory.close(player);
                    onCancel.run();
                }
        );
        contents.set(2, 6, cancelItem);
        contents.fillRect(1, 5, 3, 7, cancelItem);
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
