package net.endrealm.lostsouls.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ItemBuilder {
    private Material material;
    private String displayName;
    private int amount = 1;
    private List<Consumer<ItemMeta>> metaUpdates = new ArrayList<>();

    private ItemBuilder(Material material) {
        this.material = material;
    }

    public ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder material(Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public ItemBuilder addOnMeta(Consumer<ItemMeta> onMeta) {
        this.metaUpdates.add(onMeta);
        return this;
    }

    public static ItemBuilder builder(Material material) {
        return new ItemBuilder(material);
    }

    public ItemStack build() {
        ItemStack itemStack = new ItemStack(material);
        itemStack.setAmount(amount);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(displayName);

        metaUpdates.forEach(itemMetaConsumer -> itemMetaConsumer.accept(meta));

        itemStack.setItemMeta(meta);

        return itemStack;
    }
}
