package net.endrealm.lostsouls.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import lombok.Data;
import net.endrealm.lostsouls.Constants;
import net.endrealm.lostsouls.data.PieceType;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThemeService;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.utils.item.ItemBuilder;
import net.endrealm.lostsouls.utils.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Data
public class DraftDetails implements InventoryProvider {
    private final Draft draft;
    private final DraftService draftService;
    private final ThreadService threadService;
    private final ThemeService themeService;
    private final GuiService guiService;


    private SmartInventory smartInventory;

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)));

        int index = 0;
        if(player.hasPermission("souls_save.draft.view.other") || draft.hasMember(player.getUniqueId())){
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.COMPASS).displayName("§6Visit").build(),
                    inventoryClickEvent -> {
                        player.closeInventory();
                        if(draft.isInvalid()) {
                            player.sendMessage(Constants.ERROR_PREFIX+Constants.DRAFT_INVALIDATED);
                            return;
                        }
                        player.sendMessage(Constants.PREFIX + "Loading world!");
                        draftService.generateDraft(draft, world -> {
                                    player.teleport(world.getSpawnLocation());
                                    player.sendMessage(Constants.PREFIX + "Sent to Draft@" + draft.getId());

                                    PlayerUtils.enterBuildMode(player);
                                },
                                e -> player.sendMessage(Constants.ERROR_PREFIX + "Failed to load the world. If you think that this is an error contact your server administrator!"));
                    }));
        }
        if(player.hasPermission("souls_save.draft.delete.other") || draft.hasOwner(player.getUniqueId())){
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.BARRIER).displayName("§cDelete").build(),
                    inventoryClickEvent -> {
                        guiService.getConfirmationWindow("Delete Draft@"+draft.getId(),
                                () -> draftService.deleteDraft(draft, () -> player.sendMessage(Constants.PREFIX+"Draft " +draft.getId() + " was deleted!")),
                                () -> smartInventory.open(player)).open(player);
                    }));
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
                    inventoryClickEvent -> {
                        player.closeInventory();
                        player.sendMessage("TODO: add publish gui");
                        themeService.loadTheme("old_dungeon", theme -> {
                            draftService.publishNew(PieceType.PATH, theme, draft, piece -> {
                                        player.sendMessage("Now at " + piece.getTheme() +"/"+piece.getPieceType().toString().toLowerCase()+"/"+piece.getNumber());
                                    },
                                    () -> {
                                        player.sendMessage("Error");
                                    });
                        }, () -> {player.sendMessage("no theme old_dungeon");});
                    }
                )
            );
        }

        if((player.hasPermission("souls_save.draft.manage_members.own") && draft.hasOwner(player.getUniqueId())) || player.hasPermission("souls_save.draft.manage_members.other")) {
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.PLAYER_HEAD).displayName("§bEdit Users").build(),
                    inventoryClickEvent -> guiService.getEditDraftMembers(draft, () -> smartInventory.open(player)).open(player)));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
