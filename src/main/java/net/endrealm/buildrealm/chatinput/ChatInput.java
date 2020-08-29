package net.endrealm.buildrealm.chatinput;

import org.bukkit.entity.Player;

public interface ChatInput {
    void handle(Player player, String input);

    String validate(Player player, String input);

    String getQuestion(Player player);
}
