package net.endrealm.lostsouls.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.endrealm.lostsouls.Constants;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.data.entity.Theme;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThemeService;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class Publish implements InventoryProvider {
    private final Draft draft;
    private final Runnable onCancel;

    private final DraftService draftService;
    private final ThemeService themeService;
    private final ThreadService threadService;
    private final DataProvider dataProvider;
    private final GuiService guiService;
    @Setter
    private SmartInventory smartInventory;

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fill(ClickableItem.empty(ItemBuilder.builder(Material.GREEN_STAINED_GLASS_PANE).build()));

        contents.set(3, 4, ClickableItem.of(ItemBuilder.builder(Material.BARRIER).displayName("§cCancel").build(),
                inventoryClickEvent -> {
                    onCancel.run();
                }));

        if(draft.getForkData() != null) {
            contents.set(1, 1, ClickableItem.of(ItemBuilder.builder(Material.REDSTONE_BLOCK).displayName("§cReplace Original").build(),
                    inventoryClickEvent -> {
                        player.closeInventory();
                        publishReplace(player);
                    }));
            contents.set(1, 4, ClickableItem.of(ItemBuilder.builder(Material.SLIME_BLOCK).displayName("§cAs Variant").build(),
                    inventoryClickEvent -> {
                        player.closeInventory();
                        publishVariant(player);
                    }));
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
            contents.set(1, 4, ClickableItem.of(ItemBuilder.builder(Material.LIME_CONCRETE).displayName("§cPublish new").build(),
                    inventoryClickEvent -> publishNew(player)));
        }
    }

    private void publishAppend(Player player) {
        player.sendMessage(Constants.PREFIX+"Starting publish process...");
        draftService.publishAppend(draft, piece -> {
            player.sendMessage(Constants.PREFIX+"Draft@" +piece.getId() + " was published to "+piece.getTheme()+"/"+piece.getPieceType().name().toLowerCase() + "/" + piece.getEffectiveName(dataProvider));
        }, () -> {
            player.sendMessage(Constants.PREFIX+"Failed to publish. Invalid fork origin!");
        });
    }

    private void publishVariant(Player player) {
        player.sendMessage(Constants.PREFIX+"Starting publish process...");
        draftService.publishFork(draft, piece -> {
            player.sendMessage(Constants.PREFIX+"Draft@" +piece.getId() + " was published to "+piece.getTheme()+"/"+piece.getPieceType().name().toLowerCase() + "/" + piece.getEffectiveName(dataProvider));
        }, () -> {
            player.sendMessage(Constants.PREFIX+"Failed to publish. Invalid fork origin!");
        });
    }

    private void publishReplace(Player player) {
        player.sendMessage(Constants.PREFIX+"Starting publish process...");

        draftService.publishReplace(draft, piece -> {
            player.sendMessage(Constants.PREFIX+"Draft@" +piece.getId() + " was published to "+piece.getTheme()+"/"+piece.getPieceType().name().toLowerCase() + "/" + piece.getEffectiveName(dataProvider));
        }, () -> {
            player.sendMessage(Constants.PREFIX+"Failed to publish. Invalid fork origin!");
        });
    }

    private void publishNew(Player player) {
        guiService.getTypeSelection(type -> {
            guiService.getWait().open(player);
            themeService.loadAll(themes -> {
                threadService.runSync(() -> {
                    guiService.getThemesSelection(themes, theme -> {
                        player.closeInventory();
                        draftService.publishNew(type, theme, draft, piece -> {
                            player.sendMessage(Constants.PREFIX+"Draft@" +piece.getId() + " was published to "+theme.getName()+"/"+type.name().toLowerCase() + "/" + piece.getEffectiveName(dataProvider));
                            //TODO: open pubished details gui
                        }, ()-> {
                            player.sendMessage(Constants.ERROR_PREFIX+"Failed to publish the draft. If you believe that this is an error contact an administrator!");
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
