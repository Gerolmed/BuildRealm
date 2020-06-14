package net.endrealm.lostsouls.services;

import net.endrealm.lostsouls.data.entity.Draft;
import org.bukkit.World;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface DraftService {
    Draft getDraft(String id);
    List<Draft> ownedDrafts(UUID playerId);
    void createDraft(Consumer<Draft> onLoad);
    void saveDraft(Draft draft, Consumer<Draft> onLoad);
    void loadDraft(Consumer<Draft> onLoad, Consumer<Exception> onFailure);
    void generateDraft(Consumer<World> onGenerated, Consumer<Exception> onFailure);
    void replaceDraft(Draft oldDraft, Draft newDraft, Runnable onSuccess);
}
