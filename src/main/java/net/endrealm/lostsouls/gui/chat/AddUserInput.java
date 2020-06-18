package net.endrealm.lostsouls.gui.chat;

import net.endrealm.lostsouls.Constants;
import net.endrealm.lostsouls.chatinput.AbstractChatInput;
import net.endrealm.lostsouls.data.PermissionLevel;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.data.entity.Member;
import net.endrealm.lostsouls.gui.GuiService;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThreadService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class AddUserInput extends AbstractChatInput {
    private final Draft draft;
    private final Runnable onBack;
    private final boolean editable;
    private final DraftService draftService;
    private final ThreadService threadService;
    private final GuiService guiService;
    public AddUserInput(Draft draft, Runnable onBack, boolean editable, DraftService draftService, ThreadService threadService, GuiService guiService) {
        super("Type in a player's name you want to add! Or §c§l'cancel' §6 to exit input mode.");
        this.draft = draft;
        this.onBack = onBack;
        this.editable = editable;
        this.draftService = draftService;
        this.threadService = threadService;
        this.guiService = guiService;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void handle(Player player, String input) {
        player.sendMessage(Constants.PREFIX+"Player is being added...");
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);
        Member member = new Member(offlinePlayer.getUniqueId(), PermissionLevel.COLLABORATOR);
        draft.getMembers().add(member);
        draftService.saveDraft(draft, () -> threadService.runSync(() -> guiService.getEditDraftMembers(draft, onBack, editable).open(player)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public String validate(Player player, String input) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);
        if(!offlinePlayer.isOnline() || !offlinePlayer.hasPlayedBefore())
            return "Player was never on this server!";
        if(draft.hasMember(offlinePlayer.getUniqueId()))
            return "Player is already in the draft";
        return null;
    }
}
