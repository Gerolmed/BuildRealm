package net.endrealm.buildrealm.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.endrealm.buildrealm.Constants;
import net.endrealm.buildrealm.data.entity.Group;
import net.endrealm.buildrealm.data.entity.GroupSettings;
import net.endrealm.buildrealm.export.Exporter;
import net.endrealm.buildrealm.gui.GuiService;
import net.endrealm.buildrealm.repository.DataProvider;
import net.endrealm.buildrealm.services.DraftService;
import net.endrealm.buildrealm.services.GroupService;
import net.endrealm.buildrealm.services.ThreadService;
import net.endrealm.buildrealm.utils.BaseCommand;
import net.endrealm.buildrealm.utils.Observable;
import net.endrealm.buildrealm.world.WorldService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class GroupCommand extends BaseCommand {

    private final File dataFolder;
    private final GroupService groupService;
    private final DraftService draftService;
    private final ThreadService threadService;
    private final DataProvider dataProvider;
    private final WorldService worldService;
    private final GuiService guiService;
    private final List<UUID> openTransactions = Collections.synchronizedList(new ArrayList<>());
    private final Observable<Boolean> isLocked;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sendError(sender, "This is a player only command!");
            return true;
        }

        if (isLocked.get().orElse(false)) {
            sendError(sender, Constants.SYSTEM_WORKING);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            return false;
        }

        if (openTransactions.contains(player.getUniqueId())) {
            sendError(sender, Constants.TRANSACTIONS_RUNNING);
            return true;
        }

        String subCommandLabel = args[0];

        if (subCommandLabel.equalsIgnoreCase("create")) {
            if (!sender.hasPermission("souls_save.group.create")) {
                sendError(sender, Constants.NO_PERMISSION);
                return true;
            }
            if (args.length != 2) {
                sendError(sender, "Use /group create {name}");
                return true;
            }

            String newGroupName = args[1];
            if (!newGroupName.matches("[a-z_]{3,}")) {
                sendError(sender, "Group name pattern musst be: §eat least 3 long and only contain a-z and _");
                return true;
            }
            openTransactions.add(player.getUniqueId());
            sendInfo(sender, "Starting to create group " + newGroupName);
            groupService.loadGroup(newGroupName, group -> {
                openTransactions.remove(player.getUniqueId());
                sendError(sender, "Group " + group.getName() + " already exists!");
            }, () -> {
                Group group = new Group(newGroupName, false, new GroupSettings(), new ArrayList<>(), false);
                groupService.createGroup(group, createdGroup -> {
                    createdGroup.fixList();
                    openTransactions.remove(player.getUniqueId());
                    sendInfo(sender, "Group " + createdGroup.getName() + " has been created!");
                    threadService.runSync(() -> openDetails(player, createdGroup));
                }, () -> {
                    openTransactions.remove(player.getUniqueId());
                    sendError(sender, "Group " + group.getName() + " already exists!");
                });
            });
            return true;
        } else if (subCommandLabel.equalsIgnoreCase("list")) {
            if (!sender.hasPermission("souls_save.group.list")) {
                sendError(sender, Constants.NO_PERMISSION);
                return true;
            }
            openTransactions.add(player.getUniqueId());
            sendInfo(sender, "Fetching Groups...");

            groupService.loadAll(Groups -> {
                openTransactions.remove(player.getUniqueId());
                sendInfo(sender, "Opening group list...");
                threadService.runSync(() -> openList(player, Groups));
            });
            return true;
        } else if (subCommandLabel.equalsIgnoreCase("open")) {
            String groupName = "all";

            if (args.length == 2) {
                groupName = args[1];
            }
            if (!sender.hasPermission("souls_save.group.open.all") && !sender.hasPermission("souls_save.group.open." + groupName)) {
                sendError(sender, Constants.NO_PERMISSION);
                return true;
            }
            if (args.length != 2) {
                sendError(sender, "Use /group open {name}");
                return true;
            }


            openTransactions.add(player.getUniqueId());
            groupService.loadGroup(groupName,
                    group -> {
                        openTransactions.remove(player.getUniqueId());
                        sendInfo(sender, "Opening " + group.getName() + "...");
                        threadService.runSync(() -> openDetails(player, group));
                    }, () -> {
                        openTransactions.remove(player.getUniqueId());
                        sendError(sender, "The given group does not exist!");
                    });
            return true;
        } else if (subCommandLabel.equalsIgnoreCase("export")) {
            String groupName = "all";

            if (args.length == 2) {
                groupName = args[1];
            }
            if (!sender.hasPermission("souls_save.group.export.all") && !sender.hasPermission("souls_save.group.export." + groupName)) {
                sendError(sender, Constants.NO_PERMISSION);
                return true;
            }
            if (args.length != 2) {
                sendError(sender, "Use /group export {name}");
                return true;
            }


            openTransactions.add(player.getUniqueId());
            groupService.loadGroup(groupName,
                    group -> {
                        openTransactions.remove(player.getUniqueId());
                        sendInfo(sender, "Started exporting " + group.getName() + "...");
                        broadcastInfo("Started exporting a group please wait until this has finished! (You will be notified!)");
                        isLocked.next(true);
                        Exporter exporter = new Exporter(dataFolder, group, dataProvider, threadService, worldService, () -> {
                            sendInfo(sender, "Finished exporting " + group.getName() + "!");
                            broadcastInfo("Finished exporting group!");
                            isLocked.next(false);
                        });
                        threadService.runAsync(exporter);

                    }, () -> {
                        openTransactions.remove(player.getUniqueId());
                        sendError(sender, "The given group does not exist!");
                    });
            return true;
        }
        return false;
    }

    private void broadcastInfo(String message) {
        Bukkit.getOnlinePlayers().forEach(other -> {
            sendInfo(other, message);
        });
    }

    private void openList(Player player, List<Group> groups) {
        guiService.getGroupsList(groups).open(player);
    }

    private void openDetails(Player player, Group group) {
        guiService.getGroupDetails(group).open(player);
    }
}
