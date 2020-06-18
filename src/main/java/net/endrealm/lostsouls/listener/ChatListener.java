package net.endrealm.lostsouls.listener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.endrealm.lostsouls.Constants;
import net.endrealm.lostsouls.chatinput.ChatInput;
import net.endrealm.lostsouls.chatinput.ChatInputManager;
import net.endrealm.lostsouls.utils.BaseListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@EqualsAndHashCode(callSuper = true)
@Data
public class ChatListener extends BaseListener {
    private final ChatInputManager chatInputManager;

    @EventHandler(priority = EventPriority.LOWEST)
    public void preCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if(!chatInputManager.hasOpenQuestion(player.getUniqueId())) return;

        player.sendMessage(Constants.ERROR_PREFIX+"Command cancelled! Still awaiting chat input! To exit input mode type: §c§lcancel");
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void preCommand(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if(!chatInputManager.hasOpenQuestion(player.getUniqueId())) return;
        event.setCancelled(true);

        if(event.getMessage().equalsIgnoreCase("cancel")) {
            chatInputManager.removeQuestion(player.getUniqueId());
            return;
        }

        ChatInput chatInput = chatInputManager.getInput(player.getUniqueId());
        String validate = chatInput.validate(player, event.getMessage());
        if(validate != null) {
            player.sendMessage("§f> §7" + event.getMessage());
            player.sendMessage("§c§l"+validate);
            player.sendMessage("");
            player.sendMessage("§6"+chatInput.getQuestion(player));
            return;
        }
        chatInputManager.removeQuestion(player.getUniqueId());
        chatInput.handle(player, event.getMessage());
    }
}
