package net.endrealm.buildrealm.utils;

import net.endrealm.buildrealm.Constants;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class BaseCommand implements CommandExecutor {

    protected void sendError(CommandSender sender, String message) {
        sender.sendMessage(Constants.ERROR_PREFIX + message);

    }

    protected void sendInfo(CommandSender sender, String message) {
        sender.sendMessage(Constants.PREFIX + message);
    }
}
