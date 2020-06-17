package net.endrealm.lostsouls.utils.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class ItemBuilder {
    private ItemCreator itemCreator;
    private String displayName;
    private List<String> lore = new ArrayList<>();
    private int amount = 1;
    private List<Consumer<ItemMeta>> metaUpdates = new ArrayList<>();

    private ItemBuilder(ItemCreator itemCreator) {
        this.itemCreator = itemCreator;
    }

    public ItemBuilder amount(int amount) {
        this.amount = amount;
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
        return new ItemBuilder(new DefaultCreator(material));
    }

    public static ItemBuilder skullBuilder(UUID playerId) {
        return new ItemBuilder(new SkullCreator(playerId));
    }

    public ItemStack build() {
        ItemStack itemStack = itemCreator.produce();
        itemStack.setAmount(amount);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);

        metaUpdates.forEach(itemMetaConsumer -> itemMetaConsumer.accept(meta));

        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public ItemBuilder addLore(String lore) {
        this.lore.add(lore);
        return this;
    }
}
