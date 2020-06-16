package net.endrealm.lostsouls.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.endrealm.lostsouls.services.ThemeService;
import net.endrealm.lostsouls.utils.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

@EqualsAndHashCode(callSuper = true)
@Data
public class ThemeCommand extends BaseCommand {

    private final ThemeService themeService;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        return false;
    }
}
