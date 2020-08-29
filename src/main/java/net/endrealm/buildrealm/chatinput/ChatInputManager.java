package net.endrealm.buildrealm.chatinput;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatInputManager {
    private final Map<UUID, ChatInput> openInputs = new HashMap<>();

    public boolean hasOpenQuestion(UUID uuid) {
        return openInputs.containsKey(uuid);
    }

    public void removeQuestion(UUID uuid) {
        openInputs.remove(uuid);
    }

    public void addQuestion(UUID uuid, ChatInput chatInput) {
        openInputs.put(uuid, chatInput);
    }

    public ChatInput getInput(UUID uuid) {
        return openInputs.get(uuid);
    }
}
