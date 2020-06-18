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
        if(player.hasPermission("souls_save.piece.view.all") || player.hasPermission("souls_save.piece.view."+piece.getTheme())){
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
        if(player.hasPermission("souls_save.piece.delete.all") || player.hasPermission("souls_save.piece.delete."+piece.getTheme())){
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.BARRIER).displayName("§cDelete").build(),
                    inventoryClickEvent -> {
                        if(lockedInteract) return;
                        guiService.getConfirmationWindow("Delete Piece@"+ piece.getId(),
                                () -> draftService.deletePiece(piece, () -> player.sendMessage(Constants.PREFIX+"Draft " + piece.getId() + " was deleted!")),
                                () -> smartInventory.open(player)).open(player);
                    }));
        }

        if(player.hasPermission("souls_save.piece.fork.all") || player.hasPermission("souls_save.piece.fork."+piece.getTheme())){
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.TRIPWIRE_HOOK).displayName("§cCreate a Fork").build(),
                    inventoryClickEvent -> {
                        if(lockedInteract) return;
                        player.sendMessage("TODO: create fork and check if more drafts allowed");
                    }));
        }

        index++;
        contents.set(1, index, ClickableItem.of(
                ItemBuilder.builder(Material.PLAYER_HEAD).displayName("§bSee authors").build(),
                inventoryClickEvent -> {
                    if(lockedInteract) return;
                    guiService.getEditDraftMembers(piece, () -> smartInventory.open(player), false).open(player);
                }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
