package net.endrealm.lostsouls.utils;

import lombok.Data;
import net.endrealm.lostsouls.Constants;
import net.endrealm.lostsouls.LostSoulsSave;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class BaseCommand implements CommandExecutor {

    protected void sendError(CommandSender sender, String message) {
        sender.sendMessage(Constants.PREFIX + message);

    }
    protected void sendInfo(CommandSender sender, String message) {
        sender.sendMessage(Constants.ERROR_PREFIX + message);
    }
}
