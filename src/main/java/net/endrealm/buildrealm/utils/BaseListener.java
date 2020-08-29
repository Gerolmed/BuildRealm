package net.endrealm.buildrealm.utils;

import net.endrealm.buildrealm.Constants;
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
