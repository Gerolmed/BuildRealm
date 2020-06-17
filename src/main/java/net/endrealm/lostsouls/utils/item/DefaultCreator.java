package net.endrealm.lostsouls.utils.item;

import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Data
public class DefaultCreator implements ItemCreator {

    private final Material material;

    @Override
    public ItemStack produce() {
        return new ItemStack(material);
    }
}
