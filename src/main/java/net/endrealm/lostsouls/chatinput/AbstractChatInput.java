package net.endrealm.lostsouls.chatinput;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public abstract class AbstractChatInput implements ChatInput {
    @Getter
    private final String question;


    @Override
    public String getQuestion(Player player) {
        return question;
    }
}
