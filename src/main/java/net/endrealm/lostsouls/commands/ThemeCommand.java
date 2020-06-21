package net.endrealm.lostsouls.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.endrealm.lostsouls.Constants;
import net.endrealm.lostsouls.data.entity.Theme;
import net.endrealm.lostsouls.export.Exporter;
import net.endrealm.lostsouls.gui.GuiService;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThemeService;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.utils.BaseCommand;
import net.endrealm.lostsouls.utils.Observable;
import net.endrealm.lostsouls.world.WorldService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class ThemeCommand extends BaseCommand {

    private final ThemeService themeService;
    private final DraftService draftService;
    private final ThreadService threadService;
    private final DataProvider dataProvider;
    private final WorldService worldService;
    private final GuiService guiService;
    private final List<UUID> openTransactions = Collections.synchronizedList(new ArrayList<>());
    private final Observable<Boolean> isLocked;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sendError(sender, "This is a player only command!");
            return true;
        }

        if(isLocked.get().orElse(false)) {
            sendError(sender, Constants.SYSTEM_WORKING);
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

        if(subCommandLabel.equalsIgnoreCase("create")) {
            if(!sender.hasPermission("souls_save.theme.create")) {
                sendError(sender, Constants.NO_PERMISSION);
                return true;
            }
            if(args.length != 2) {
                sendError(sender, "Use /theme create {name}");
                return true;
            }

            String newThemeName = args[1];
            if(!newThemeName.matches("[a-z_]{3,}")) {
                sendError(sender, "Theme name pattern musst be: §eat least 3 long and only contain a-z and _");
                return true;
            }
            openTransactions.add(player.getUniqueId());
            sendInfo(sender, "Starting to create theme "+ newThemeName);
            themeService.loadTheme(newThemeName, theme -> {
                openTransactions.remove(player.getUniqueId());
                sendError(sender, "Theme " + theme.getName() + " already exists!");
            }, () -> {
                Theme theme = new Theme(newThemeName, false, new ArrayList<>(), false);
                themeService.createTheme(theme, createdTheme -> {
                    createdTheme.fixList();
                    openTransactions.remove(player.getUniqueId());
                    sendInfo(sender, "Theme " + createdTheme.getName() + " has been created!");
                    threadService.runSync(() -> openDetails(player, createdTheme));
                }, () -> {
                    openTransactions.remove(player.getUniqueId());
                    sendError(sender, "Theme " + theme.getName() + " already exists!");
                });
            });
            return true;
        } else if(subCommandLabel.equalsIgnoreCase("list")) {
            if(!sender.hasPermission("souls_save.theme.list")) {
                sendError(sender, Constants.NO_PERMISSION);
                return true;
            }
            openTransactions.add(player.getUniqueId());
            sendInfo(sender, "Fetching themes...");

            themeService.loadAll(themes -> {
                openTransactions.remove(player.getUniqueId());
                sendInfo(sender, "Opening theme list...");
                threadService.runSync(() -> openList(player, themes));
            });
            return true;
        } else if(subCommandLabel.equalsIgnoreCase("open")) {
            String themeName = "all";

            if(args.length == 2) {
                themeName = args[1];
            }
            if(!sender.hasPermission("souls_save.theme.open.all") && !sender.hasPermission("souls_save.theme.open."+themeName)) {
                sendError(sender, Constants.NO_PERMISSION);
                return true;
            }
            if(args.length != 2) {
                sendError(sender, "Use /theme open {name}");
                return true;
            }


            openTransactions.add(player.getUniqueId());
            themeService.loadTheme(themeName,
            theme -> {
                openTransactions.remove(player.getUniqueId());
                sendInfo(sender, "Opening " + theme.getName() + "...");
                threadService.runSync(() -> openDetails(player, theme));
            }, () -> {
                 openTransactions.remove(player.getUniqueId());
                 sendError(sender, "The given theme does not exist!");
            });
            return true;
        } else if(subCommandLabel.equalsIgnoreCase("export")) {
            String themeName = "all";

            if(args.length == 2) {
                themeName = args[1];
            }
            if(!sender.hasPermission("souls_save.theme.export.all") && !sender.hasPermission("souls_save.theme.export."+themeName)) {
                sendError(sender, Constants.NO_PERMISSION);
                return true;
            }
            if(args.length != 2) {
                sendError(sender, "Use /theme export {name}");
                return true;
            }


            openTransactions.add(player.getUniqueId());
            themeService.loadTheme(themeName,
                    theme -> {
                        openTransactions.remove(player.getUniqueId());
                        sendInfo(sender, "Started exporting " + theme.getName() + "...");
                        broadcastInfo("Started exporting a theme please wait until this has finished! (You will be notified!)");
                        isLocked.next(true);
                        Exporter exporter = new Exporter(theme, dataProvider, threadService, worldService, () -> {
                            sendInfo(sender, "Finished exporting " + theme.getName() + "!");
                            broadcastInfo("Finished exporting theme!");
                        });
                        threadService.runAsync(exporter::run);

                    }, () -> {
                        openTransactions.remove(player.getUniqueId());
                        sendError(sender, "The given theme does not exist!");
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

    private void openList(Player player, List<Theme> themes) {
        guiService.getThemesList(themes).open(player);
    }

    private void openDetails(Player player, Theme theme) {
        guiService.getThemeDetails(theme).open(player);
    }
}
