package net.endrealm.lostsouls.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import lombok.Data;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Data
public class DraftDetails implements InventoryProvider {
    private final Player player;
    private final Draft draft;

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)));

        int index = 0;
        if(player.hasPermission("souls_save.draft.view.other") || draft.hasMember(player.getUniqueId())){
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.COMPASS).displayName("§6Visit").build(),
                    inventoryClickEvent -> player.sendMessage("TODO: add editing")));
        }
        if(player.hasPermission("souls_save.draft.delete.other") || draft.hasOwner(player.getUniqueId())){
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.BARRIER).displayName("§cDelete").build(),
                    inventoryClickEvent -> player.sendMessage("TODO: add deletion")));
        }

        if(player.hasPermission("souls_save.draft.change_theme.other") || draft.hasOwner(player.getUniqueId())){
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.GRASS_BLOCK).displayName("§6Change Theme").build(),
                    inventoryClickEvent -> player.sendMessage("TODO: add theme change")));
        }

        if(player.hasPermission("souls_save.draft.unfork.other") || draft.hasOwner(player.getUniqueId())){
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.TRIPWIRE_HOOK).displayName("§cUnfork").build(),
                    inventoryClickEvent -> player.sendMessage("TODO: add unforking")));
        }

        if(player.hasPermission("souls_save.draft.publish.other") ||
                (
                        draft.hasOwner(player.getUniqueId()) &&
                        player.hasPermission("souls_save.draft.publish.own")
                )
        ){
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.WRITABLE_BOOK).displayName("§aPublish").build(),
                    inventoryClickEvent -> player.sendMessage("TODO: add publish gui")));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
