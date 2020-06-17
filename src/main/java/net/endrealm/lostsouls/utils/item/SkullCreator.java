package net.endrealm.lostsouls.utils.item;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

@Data
public class SkullCreator implements ItemCreator {

    private final UUID playerId;

    @Override
    public ItemStack produce() {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerId));
        stack.setItemMeta(meta);
        return stack;
    }
}
