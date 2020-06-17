package net.endrealm.lostsouls.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.endrealm.lostsouls.Constants;
import net.endrealm.lostsouls.data.entity.Theme;
import net.endrealm.lostsouls.services.ThemeService;
import net.endrealm.lostsouls.utils.BaseCommand;
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
    private final List<UUID> openTransactions = Collections.synchronizedList(new ArrayList<>());

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
            if(newThemeName.matches("[a-z_]{3,}")) {
                sendError(sender, "Theme name pattern musst be: §eat least 3 long and only contain a-z and _");
                return true;
            }
            openTransactions.add(player.getUniqueId());
            sendInfo(sender, "Starting to create theme "+ newThemeName);
            themeService.loadTheme(newThemeName, theme -> {
                openTransactions.remove(player.getUniqueId());
                sendError(sender, "Theme " + theme.getName() + " already exists!");
            }, () -> {
                Theme theme = new Theme(newThemeName, false, false);
                themeService.createTheme(theme, createdTheme -> {
                    openTransactions.remove(player.getUniqueId());
                    sendInfo(sender, "Theme " + theme.getName() + " has been created!");
                    //TODO: open theme details gui
                }, () -> {
                    sendError(sender, "Theme " + theme.getName() + " already exists!");
                });
            });
            return true;
        }
        return false;
    }
}
