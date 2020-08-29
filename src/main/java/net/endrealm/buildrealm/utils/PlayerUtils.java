package net.endrealm.buildrealm.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlayerUtils {
    public static void enterBuildMode(Player player) {
        player.setFlying(true);
        player.setGameMode(GameMode.CREATIVE);
    }
}
