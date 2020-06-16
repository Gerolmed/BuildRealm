package net.endrealm.lostsouls.utils;

import net.endrealm.lostsouls.Constants;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

public abstract class BaseListener implements Listener {

    protected void sendError(CommandSender sender, String message) {
        sender.sendMessage(Constants.ERROR_PREFIX + message);

    }
    protected void sendInfo(CommandSender sender, String message) {
        sender.sendMessage(Constants.PREFIX + message);
    }
}
