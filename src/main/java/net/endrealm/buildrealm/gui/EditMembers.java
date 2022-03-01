package net.endrealm.buildrealm.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import lombok.Data;
import net.endrealm.buildrealm.Constants;
import net.endrealm.buildrealm.chatinput.ChatInputManager;
import net.endrealm.buildrealm.data.entity.Draft;
import net.endrealm.buildrealm.data.entity.Member;
import net.endrealm.buildrealm.gui.chat.AddUserInput;
import net.endrealm.buildrealm.services.DraftService;
import net.endrealm.buildrealm.services.ThreadService;
import net.endrealm.buildrealm.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

@Data
public class EditMembers implements InventoryProvider {
    private final Draft draft;
    private final DraftService draftService;
    private final ThreadService threadService;
    private final GuiService guiService;
    private final Runnable onBack;
    private final boolean editable;
    private final ChatInputManager chatInputManager;
    private SmartInventory smartInventory;

    private boolean locked;

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(ItemBuilder.builder(Material.GREEN_STAINED_GLASS_PANE).build()));

        Pagination pagination = contents.pagination();
        pagination.setItemsPerPage(14);
        pagination.setItems(draft.getMembers().stream().map(
                member -> ClickableItem.of(
                        ItemBuilder
                                .skullBuilder(member.getUuid())
                                .displayName("§6" + Bukkit.getOfflinePlayer(member.getUuid()).getName())
                                .addLore("§7" + member.getPermissionLevel())
                                .build(),
                        inventoryClickEvent -> {
                            if (!editable) return;
                            handleUserClick(player, member, inventoryClickEvent.getClick(), pagination);
                            inventoryClickEvent.setCancelled(true);
                        }
                )
        ).toArray(ClickableItem[]::new));

        ClickableItem[] items = pagination.getPageItems();
        int half = items.length / 2;
        for (int i = 0; i < half; i++) {
            contents.set(1, i + 1, items[i]);
            contents.set(2, i + 1, items[i + half]);
        }

        if (editable) {
            contents.set(0, 4, ClickableItem.of(ItemBuilder.builder(Material.EMERALD).displayName("§aAdd player").build(), inventoryClickEvent -> {
                inventoryClickEvent.setCancelled(true);
                if (!(player.hasPermission("build_realm.draft.manage_members.add.own") && draft.hasOwner(player.getUniqueId())) && !player.hasPermission("build_realm.draft.manage_members.add.other")) {
                    return;
                }

                player.closeInventory();
                chatInputManager.addQuestion(player.getUniqueId(), new AddUserInput(draft, onBack, editable, draftService, threadService, guiService));
                player.sendMessage("§6" + chatInputManager.getInput(player.getUniqueId()).getQuestion(player));
            }));
        }

        contents.set(3, 4, ClickableItem.of(ItemBuilder.builder(Material.SLIME_BALL).displayName("§cBack").build(), inventoryClickEvent -> onBack.run()));

        if (!pagination.isFirst())
            contents.set(3, 3, ClickableItem.of(ItemBuilder.builder(Material.ARROW).displayName("§6Previous Page").build(),
                    e -> smartInventory.open(player, pagination.previous().getPage())));
        if (!pagination.isLast())
            contents.set(3, 5, ClickableItem.of(ItemBuilder.builder(Material.ARROW).displayName("§6Next Page").build(),
                    e -> smartInventory.open(player, pagination.next().getPage())));
    }

    private void handleUserClick(Player player, Member member, ClickType click, Pagination pagination) {
        if (draft.isInvalid()) {
            player.sendMessage(Constants.PREFIX + Constants.DRAFT_INVALIDATED);
            player.closeInventory();
            return;
        }

        if (click == ClickType.LEFT) {
            toggle(player, member, pagination);
        } else if (click == ClickType.RIGHT) {
            remove(player, member, pagination);
        }
    }

    private void remove(Player player, Member member, Pagination pagination) {
        if (!(player.hasPermission("build_realm.draft.manage_members.add.own") && draft.hasOwner(player.getUniqueId())) && !player.hasPermission("build_realm.draft.manage_members.add.other")) {
            return;
        }

        boolean editAll = player.hasPermission("build_realm.draft.manage_members.other");
        if (!(player.hasPermission("build_realm.draft.manage_members.own") && draft.hasOwner(player.getUniqueId())) && !editAll) {
            player.closeInventory();
            return;
        }

        if (player.getUniqueId().equals(member.getUuid()) && !editAll) {
            return;
        }

        locked = true;
        draft.getMembers().remove(member);

        draftService.saveDraft(draft, () -> threadService.runSync(() -> guiService.getEditDraftMembers(draft, onBack, editable).open(player, pagination.getPage())));
    }

    private void toggle(Player player, Member member, Pagination pagination) {
        if (!(player.hasPermission("build_realm.draft.manage_members.toggle.own") && draft.hasOwner(player.getUniqueId())) && !player.hasPermission("build_realm.draft.manage_members.toggle.other")) {
            return;
        }

        boolean editAll = player.hasPermission("build_realm.draft.manage_members.other");
        if (!(player.hasPermission("build_realm.draft.manage_members.own") && draft.hasOwner(player.getUniqueId())) && !editAll) {
            player.closeInventory();
            return;
        }

        if (player.getUniqueId().equals(member.getUuid()) && !editAll) {
            return;
        }

        locked = true;
        member.setPermissionLevel(member.getPermissionLevel().cycle());

        draftService.saveDraft(draft, () -> threadService.runSync(() -> guiService.getEditDraftMembers(draft, onBack, editable).open(player, pagination.getPage())));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
