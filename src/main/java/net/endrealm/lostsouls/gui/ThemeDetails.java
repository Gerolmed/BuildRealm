package net.endrealm.lostsouls.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import lombok.Data;
import net.endrealm.lostsouls.Constants;
import net.endrealm.lostsouls.data.PieceType;
import net.endrealm.lostsouls.data.entity.Piece;
import net.endrealm.lostsouls.data.entity.Theme;
import net.endrealm.lostsouls.data.entity.TypeCategory;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThemeService;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ThemeDetails implements InventoryProvider {
    private final DraftService draftService;
    private final ThemeService themeService;
    private final GuiService guiService;
    private final ThreadService threadService;
    private final Theme theme;
    private SmartInventory smartInventory;

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(ItemBuilder.builder(Material.GREEN_STAINED_GLASS_PANE).build()));

        // Can edit theme
        if(player.hasPermission("souls_save.theme.edit.all") || player.hasPermission("souls_save.theme.edit."+theme.getName())) {
            contents.set(0, 2, ClickableItem.of(ItemBuilder.builder(Material.REPEATER).displayName("§aSettings").build(),
                    inventoryClickEvent -> {
                        player.sendMessage("todo: open settings");
                    })
            );
        }

        contents.set(0, 4, ClickableItem.empty(
                ItemBuilder
                        .builder(Material.BOOK)
                        .displayName("§6Theme@§e"+theme.getName())
                        .addLore("§bIsComplete: " + (theme.isComplete() ? "§ayes" : "§cno"))
                        .addLore("§bStale: " + (theme.isStale() ? "§ayes" : "§cno"))
                        .build()));

        // Can delete theme
        if(player.hasPermission("souls_save.theme.delete.all") || player.hasPermission("souls_save.theme.delete."+theme.getName())) {
            contents.set(0, 6, ClickableItem.of(ItemBuilder.builder(Material.BARRIER).displayName("§cDelete").build(),
                    inventoryClickEvent -> {
                        guiService.getConfirmationWindow("Delete theme " + theme.getName(),
                                () -> {
                                    themeService.deleteTheme(theme, () -> player.sendMessage(Constants.PREFIX+"Theme " +theme.getName() + " was deleted! Its drafts are now floating (/draft floating)"));
                                },
                                () -> smartInventory.open(player)).open(player);
                    })
            );
        }
        List<TypeCategory> categories = new ArrayList<>(theme.getCategoryList());
        categories.sort(Comparator.comparingInt(o -> o.getType().ordinal()));
        for (int i = 0; i < categories.size(); i++) {
            int col = i < 7 ? i : i - 7;
            int row = i < 7 ? 1 : 2;
            TypeCategory category = categories.get(i);
            PieceType type = category.getType();
            contents.set(row, col+1, ClickableItem.of(
                    ItemBuilder.builder(type.getMaterial()).displayName("§6"+type.getDisplayName()).addLore("§aCount: §7"+category.getPieceCount()).build(),
                    inventoryClickEvent -> draftService.draftsByThemeAndType(theme, category.getType(), drafts -> threadService.runSync(()-> guiService.getCategoryDetails(theme, category, drafts.stream().map(draft -> (Piece) draft).collect(Collectors.toList())).open(player)))
            ));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
