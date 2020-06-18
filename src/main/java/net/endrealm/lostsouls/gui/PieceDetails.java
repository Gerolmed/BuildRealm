package net.endrealm.lostsouls.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import lombok.Data;
import net.endrealm.lostsouls.Constants;
import net.endrealm.lostsouls.data.entity.Piece;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThemeService;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.utils.PlayerUtils;
import net.endrealm.lostsouls.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Data
public class PieceDetails implements InventoryProvider {
    private final Piece piece;
    private final DraftService draftService;
    private final ThreadService threadService;
    private final ThemeService themeService;
    private final GuiService guiService;


    private SmartInventory smartInventory;
    private boolean lockedInteract;

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)));

        int index = 0;
        if(player.hasPermission("souls_save.draft.view.other") || piece.hasMember(player.getUniqueId())){
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.COMPASS).displayName("§6Visit").build(),
                    inventoryClickEvent -> {
                        if(lockedInteract) return;

                        player.closeInventory();
                        if(piece.isInvalid()) {
                            player.sendMessage(Constants.ERROR_PREFIX+Constants.DRAFT_INVALIDATED);
                            return;
                        }
                        player.sendMessage(Constants.PREFIX + "Loading world!");
                        draftService.generateDraft(piece, world -> {
                                    player.teleport(world.getSpawnLocation());
                                    player.sendMessage(Constants.PREFIX + "Sent to Draft@" + piece.getId());

                                    PlayerUtils.enterBuildMode(player);
                                },
                                e -> player.sendMessage(Constants.ERROR_PREFIX + "Failed to load the world. If you think that this is an error contact your server administrator!"));
                    }));
        }
        if(player.hasPermission("souls_save.draft.delete.other") || piece.hasOwner(player.getUniqueId())){
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.BARRIER).displayName("§cDelete").build(),
                    inventoryClickEvent -> {
                        if(lockedInteract) return;
                        guiService.getConfirmationWindow("Delete Draft@"+ piece.getId(),
                                () -> draftService.deleteDraft(piece, () -> player.sendMessage(Constants.PREFIX+"Draft " + piece.getId() + " was deleted!")),
                                () -> smartInventory.open(player)).open(player);
                    }));
        }

        if(player.hasPermission("souls_save.draft.unfork.other") || piece.hasOwner(player.getUniqueId())){
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.TRIPWIRE_HOOK).displayName("§cUnfork").build(),
                    inventoryClickEvent -> {
                        if(lockedInteract) return;
                        player.sendMessage("TODO: add unforking");
                    }));
        }

        if(player.hasPermission("souls_save.draft.publish.other") ||
                (
                        piece.hasOwner(player.getUniqueId()) &&
                        player.hasPermission("souls_save.draft.publish.own")
                )
        ){
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.WRITABLE_BOOK).displayName("§aPublish").build(),
                    inventoryClickEvent -> {
                        if(lockedInteract) return;

                        lockedInteract = true;
                        guiService.getPublishDraft(piece, () -> threadService.runSync(()->smartInventory.open(player))).open(player);
                    }
                )
            );
        }

        if((player.hasPermission("souls_save.draft.manage_members.own") && piece.hasOwner(player.getUniqueId())) || player.hasPermission("souls_save.draft.manage_members.other")) {
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.PLAYER_HEAD).displayName("§bEdit Users").build(),
                    inventoryClickEvent -> {
                        if(lockedInteract) return;
                        guiService.getEditDraftMembers(piece, () -> smartInventory.open(player)).open(player);
                    }));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
