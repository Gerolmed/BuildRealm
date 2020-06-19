package net.endrealm.lostsouls.services;

import org.bukkit.entity.Player;

import java.util.function.Consumer;

public interface PermissionService {
    int maxDrafts(Player player);
    void currentDrafts(Player player, Consumer<Integer> onLoad);
}
