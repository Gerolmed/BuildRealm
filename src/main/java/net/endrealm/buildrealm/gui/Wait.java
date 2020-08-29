package net.endrealm.buildrealm.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import lombok.RequiredArgsConstructor;
import net.endrealm.buildrealm.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class Wait implements InventoryProvider {

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fill(ClickableItem.empty(ItemBuilder.builder(Material.GREEN_STAINED_GLASS_PANE).build()));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
