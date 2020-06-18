package net.endrealm.lostsouls.commands;

import lombok.RequiredArgsConstructor;
import net.endrealm.lostsouls.Constants;
import net.endrealm.lostsouls.data.PermissionLevel;
import net.endrealm.lostsouls.data.entity.Member;
import net.endrealm.lostsouls.gui.GuiService;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.utils.BaseCommand;
import net.endrealm.lostsouls.world.WorldIdentity;
import net.endrealm.lostsouls.world.WorldService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

@RequiredArgsConstructor
public class DraftCommand extends BaseCommand {

    private final DraftService draftService;
    private final ThreadService threadService;
    private final WorldService worldService;
    private final GuiService guiService;

    private final List<UUID> openTransactions = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)) {
            sendError(sender, "This is a player only command!");
            return true;
        }
        Player player = (Player) sender;

        if(args.length == 0) {
            return false;
        }

        if(openTransactions.contains(player.getUniqueId())) {
            sendError(sender, Constants.TRANSACTIONS_RUNNING);
            return true;
        }

        String subCommandLabel = args[0];
        if(subCommandLabel.equalsIgnoreCase("list")) {
            if(args.length == 1) {
                if(!sender.hasPermission("souls_save.draft.list.own")) {
                    sendError(sender, Constants.NO_PERMISSION);
                    return true;
                }
                return onList(player, player);
            }

            if(!sender.hasPermission("souls_save.draft.list.other")) {
                sendError(sender, Constants.NO_PERMISSION);
                return true;
            }
            String playerName = args[1];
            OfflinePlayer offlinePlayer = getOfflinePlayer(playerName);

            if(offlinePlayer == null) {
                sendError(sender, "The player " + playerName + " has never played on this server!");
                return true;
            }
            return onList(player, offlinePlayer);
        } else if(subCommandLabel.equalsIgnoreCase("create")) {
            if(!sender.hasPermission("souls_save.draft.create")) {
                sendError(sender, Constants.NO_PERMISSION);
                return true;
            }

            StringBuilder noteBuilder = new StringBuilder();
            String note = "";
            if(args.length > 1) {
                for (int i = 1; i < args.length; i++) {
                    noteBuilder.append(" ").append(args[i]);
                }
                note =  noteBuilder.substring(1);
            }
            tryCreate(player, note);
            return true;
        } else if(subCommandLabel.equalsIgnoreCase("leave")) {
            if(!sender.hasPermission("souls_save.draft.leave")) {
                sendError(sender, Constants.NO_PERMISSION);
                return true;
            }
            if(!worldService.isLoaded(new WorldIdentity(player.getWorld().getName(), false))) {
                sendError(sender, "You are not in a Draft!");
                return true;
            }

            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            return true;
        } else if(subCommandLabel.equalsIgnoreCase("floating")) {
            if (!sender.hasPermission("souls_save.draft.floating")) {
                sendError(sender, Constants.NO_PERMISSION);
                return true;
            }

            sendInfo(sender, "We are still working on this feature. Hope we can add it in soon");
            return true;
        }
        return false;
    }

    private void tryCreate(Player player, String note) {
        openTransactions.add(player.getUniqueId());
        final int maxDrafts = getMaxDrafts(player);
        sendInfo(player, "Preparing to create a new draft...");
        draftService.ownedDrafts(player.getUniqueId(), drafts -> {
            openTransactions.remove(player.getUniqueId());
        });


        draftService.ownedDrafts(player.getUniqueId(), drafts -> {
            openTransactions.remove(player.getUniqueId());

            if(drafts.size() >= maxDrafts) {
                threadService.runSync(() -> sendError(player, "You already have to many open drafts. Max."+maxDrafts));
                return;
            }
            draftService.createDraft(draft -> {
                draft.setNote(note);
                draft.setMembers(new ArrayList<>(Collections.singletonList(new Member(player.getUniqueId(), PermissionLevel.OWNER))));
                draftService.saveDraft(draft,() -> {
                    threadService.runSync(() -> {
                        sendInfo(player, "Created a new draft " + draft.getId());
                        guiService.getDraftDetails(draft).open(player);
                    });
                });
            }, () -> {
                sendError(player, "An error occurred while saving! Please report this to an administrator");
            });
        });
    }

    @SuppressWarnings("deprecation")
    private OfflinePlayer getOfflinePlayer(String name) {
        OfflinePlayer offlinePlayer = Bukkit.getPlayer(name);

        if(offlinePlayer == null) {
            offlinePlayer = Bukkit.getOfflinePlayer(name);
            if(!offlinePlayer.hasPlayedBefore())
                offlinePlayer = null;
        }

        return offlinePlayer;
    }

    private boolean onList(Player player, OfflinePlayer target) {
        openTransactions.add(player.getUniqueId());
        sendInfo(player, "Opening drafts owned by " + target.getName()+"...");


        draftService.ownedDrafts(target.getUniqueId(), drafts -> {
            openTransactions.remove(player.getUniqueId());
            threadService.runSync(() -> guiService.getDraftsList(target, drafts).open(player));
        });
        return true;
    }

    private int getMaxDrafts(Player player) {
        //TODO: add check for max drafts
        return 5;
    }
}
