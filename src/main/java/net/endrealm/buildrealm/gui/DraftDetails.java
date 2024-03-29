package net.endrealm.buildrealm.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import lombok.Data;
import net.endrealm.buildrealm.Constants;
import net.endrealm.buildrealm.data.entity.Draft;
import net.endrealm.buildrealm.data.entity.Piece;
import net.endrealm.buildrealm.services.DraftService;
import net.endrealm.buildrealm.services.GroupService;
import net.endrealm.buildrealm.services.ThreadService;
import net.endrealm.buildrealm.utils.PlayerUtils;
import net.endrealm.buildrealm.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Data
public class DraftDetails implements InventoryProvider {
    private final Draft draft;
    private final DraftService draftService;
    private final ThreadService threadService;
    private final GroupService GroupService;
    private final GuiService guiService;


    private SmartInventory smartInventory;
    private boolean lockedInteract;
    private Draft parent;
    private boolean parentSet;

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)));

        int index = 0;
        if (player.hasPermission("build_realm.draft.view.other") || draft.hasMember(player.getUniqueId())) {
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.COMPASS).displayName("§6Visit").build(),
                    inventoryClickEvent -> {
                        if (lockedInteract) return;

                        player.closeInventory();
                        if (draft.isInvalid()) {
                            player.sendMessage(Constants.ERROR_PREFIX + Constants.DRAFT_INVALIDATED);
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
        if (player.hasPermission("build_realm.draft.delete.other") || draft.hasOwner(player.getUniqueId())) {
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.BARRIER).displayName("§cDelete").build(),
                    inventoryClickEvent -> {
                        if (lockedInteract) return;
                        guiService.getConfirmationWindow("Delete Draft@" + draft.getId(),
                                () -> draftService.deleteDraft(draft, () -> player.sendMessage(Constants.PREFIX + "Draft " + draft.getId() + " was deleted!")),
                                () -> smartInventory.open(player)).open(player);
                    }));
        }

        if (draft.getForkData() != null && (player.hasPermission("build_realm.draft.unfork.other") || draft.hasOwner(player.getUniqueId()))) {
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.TRIPWIRE_HOOK).displayName("§cUnfork").build(),
                    inventoryClickEvent -> {
                        if (lockedInteract) return;
                        lockedInteract = true;
                        guiService.getConfirmationWindow("Unfork (Cannot be undone)", () -> {
                            draft.setForkData(null);
                            draftService.saveDraft(draft, () ->
                                    threadService.runSync(() -> {
                                        lockedInteract = false;
                                        guiService.getDraftDetails(draft).open(player);
                                    }));
                        }, () -> {
                            lockedInteract = false;
                            smartInventory.open(player);
                        }).open(player);
                    }));
        }

        if (player.hasPermission("build_realm.draft.publish.other") ||
                (
                        draft.hasOwner(player.getUniqueId()) &&
                                player.hasPermission("build_realm.draft.publish.own")
                )
        ) {
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.WRITABLE_BOOK).displayName("§aPublish").build(),
                    inventoryClickEvent -> {
                        if (lockedInteract) return;

                        lockedInteract = true;
                        guiService.getPublishDraft(draft, () -> threadService.runSync(() -> smartInventory.open(player))).open(player);
                    }
                    )
            );
        }

        if ((player.hasPermission("build_realm.draft.manage_members.own") && draft.hasOwner(player.getUniqueId())) || player.hasPermission("build_realm.draft.manage_members.other")) {
            index++;
            contents.set(1, index, ClickableItem.of(
                    ItemBuilder.builder(Material.PLAYER_HEAD).displayName("§bEdit Users").build(),
                    inventoryClickEvent -> {
                        if (lockedInteract) return;
                        guiService.getEditDraftMembers(draft, () -> smartInventory.open(player), true).open(player);
                    }));
        }
        if (draft.getForkData() != null) {
            draftService.loadDraft(draft.getForkData().getOriginId(),
                    draft1 -> {
                        parent = draft1;
                    },
                    () -> {
                        draft.setForkData(null);
                        draftService.saveDraft(draft, () -> {
                            threadService.runSync(() -> {
                                guiService.getDraftDetails(draft).open(player);
                            });
                        });
                    }
            );
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        if (parent != null && !parentSet) {
            parentSet = true;
            contents.set(1, 7, ClickableItem.of(ItemBuilder.builder(Material.CHISELED_STONE_BRICKS).displayName("§6View parent").build(),
                    inventoryClickEvent -> {
                        if (parent instanceof Piece) {
                            guiService.getPieceDetails((Piece) parent).open(player);
                            return;
                        }
                        guiService.getDraftDetails(parent).open(player);
                    }));
        }
    }
}
