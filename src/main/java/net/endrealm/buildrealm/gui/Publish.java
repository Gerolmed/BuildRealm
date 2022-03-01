package net.endrealm.buildrealm.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.endrealm.buildrealm.Constants;
import net.endrealm.buildrealm.data.entity.Draft;
import net.endrealm.buildrealm.repository.DataProvider;
import net.endrealm.buildrealm.services.DraftService;
import net.endrealm.buildrealm.services.GroupService;
import net.endrealm.buildrealm.services.ThreadService;
import net.endrealm.buildrealm.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class Publish implements InventoryProvider {
    private final Draft draft;
    private final Runnable onCancel;

    private final DraftService draftService;
    private final GroupService GroupService;
    private final ThreadService threadService;
    private final DataProvider dataProvider;
    private final GuiService guiService;
    @Setter
    private SmartInventory smartInventory;

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fill(ClickableItem.empty(ItemBuilder.builder(Material.GREEN_STAINED_GLASS_PANE).build()));

        boolean allPerm = player.hasPermission("build_realm.publish.any");
        contents.set(3, 4, ClickableItem.of(ItemBuilder.builder(Material.BARRIER).displayName("§cCancel").build(),
                inventoryClickEvent -> {
                    onCancel.run();
                }));

        if (draft.getForkData() != null) {

            if (allPerm || player.hasPermission("build_realm.publish.original." + draft.getGroup()) || player.hasPermission("build_realm.publish.original"))
                contents.set(1, 1, ClickableItem.of(ItemBuilder.builder(Material.REDSTONE_BLOCK).displayName("§cReplace Original").build(),
                        inventoryClickEvent -> {
                            player.closeInventory();
                            publishReplace(player);
                        }));
            if (allPerm || player.hasPermission("build_realm.publish.variant." + draft.getGroup()) || player.hasPermission("build_realm.publish.variant"))
                contents.set(1, 4, ClickableItem.of(ItemBuilder.builder(Material.SLIME_BLOCK).displayName("§cAs Variant").build(),
                        inventoryClickEvent -> {
                            player.closeInventory();
                            publishVariant(player);
                        }));
            if (allPerm || player.hasPermission("build_realm.publish.original_new." + draft.getGroup()) || player.hasPermission("build_realm.publish.original_new"))
                contents.set(1, 7, ClickableItem.of(
                        ItemBuilder
                                .builder(Material.EMERALD_BLOCK)
                                .displayName("§cAppend")
                                .build()
                        ,
                        inventoryClickEvent -> {
                            player.closeInventory();
                            publishAppend(player);
                        }));
        } else {
            if (allPerm || player.hasPermission("build_realm.publish.new." + draft.getGroup()) || player.hasPermission("build_realm.publish.new"))
                contents.set(1, 4, ClickableItem.of(ItemBuilder.builder(Material.LIME_CONCRETE).displayName("§cPublish new").build(),
                        inventoryClickEvent -> publishNew(player)));
        }
    }

    private void publishAppend(Player player) {
        player.sendMessage(Constants.PREFIX + "Starting publish process...");
        draftService.publishAppend(draft, piece -> {
            player.sendMessage(Constants.PREFIX + "Draft@" + piece.getId() + " was published to " + piece.getGroup() + "/" + piece.getPieceType().name().toLowerCase() + "/" + piece.getEffectiveName(dataProvider));
            threadService.runSync(() -> {
                guiService.getPieceDetails(piece).open(player);
            });
        }, () -> {
            player.sendMessage(Constants.PREFIX + "Failed to publish. Invalid fork origin!");
        });
    }

    private void publishVariant(Player player) {
        player.sendMessage(Constants.PREFIX + "Starting publish process...");
        draftService.publishFork(draft, piece -> {
            player.sendMessage(Constants.PREFIX + "Draft@" + piece.getId() + " was published to " + piece.getGroup() + "/" + piece.getPieceType().name().toLowerCase() + "/" + piece.getEffectiveName(dataProvider));
            threadService.runSync(() -> {
                guiService.getPieceDetails(piece).open(player);
            });
        }, () -> {
            player.sendMessage(Constants.ERROR_PREFIX + "Failed to publish. Invalid fork origin!");
        });
    }

    private void publishReplace(Player player) {
        player.sendMessage(Constants.PREFIX + "Starting publish process...");

        draftService.publishReplace(draft, piece -> {
            player.sendMessage(Constants.PREFIX + "Draft@" + piece.getId() + " was published to " + piece.getGroup() + "/" + piece.getPieceType().name().toLowerCase() + "/" + piece.getEffectiveName(dataProvider));
            threadService.runSync(() -> {
                guiService.getPieceDetails(piece).open(player);
            });
        }, () -> {
            player.sendMessage(Constants.ERROR_PREFIX + "Failed to publish. Invalid fork origin!");
        });
    }

    private void publishNew(Player player) {
        guiService.getTypeSelection(type -> {
                    guiService.getWait().open(player);
                    GroupService.loadAll(Groups -> {
                        threadService.runSync(() -> {
                            guiService.getGroupsSelection(Groups, Group -> {
                                player.closeInventory();
                                draftService.publishNew(type, Group, draft, piece -> {
                                    player.sendMessage(Constants.PREFIX + "Draft@" + piece.getId() + " was published to " + Group.getName() + "/" + type.name().toLowerCase() + "/" + piece.getEffectiveName(dataProvider));
                                    threadService.runSync(() -> {
                                        guiService.getPieceDetails(piece).open(player);
                                    });
                                }, () -> {
                                    player.sendMessage(Constants.ERROR_PREFIX + "Failed to publish the draft. If you believe that this is an error contact an administrator!");
                                });
                            }, onCancel).open(player);
                        });
                    });
                }, onCancel
        ).open(player);
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
