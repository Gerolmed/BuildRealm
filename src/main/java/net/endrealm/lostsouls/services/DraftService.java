package net.endrealm.lostsouls.services;

import net.endrealm.lostsouls.data.entity.Draft;
import org.bukkit.World;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface DraftService {
    void loadDraft(String id, Consumer<Draft> onLoad, Runnable notExists);
    void ownedDrafts(UUID playerId, Consumer<List<Draft>> onLoad);
    void createDraft(Consumer<Draft> onLoad, Runnable duplicateId);
    void saveDraft(Draft draft, Runnable onLoad);
    void unloadDraft(Draft draft, Runnable onFinish, Consumer<Exception> onFailure);

    /**
     * Generates a draft or finds currently generated draft
     *
     */
    void generateDraft(Draft draft, Consumer<World> onGenerated, Consumer<Exception> onFailure);
    void replaceDraft(Draft oldDraft, Draft newDraft, Runnable onSuccess);

    void deleteDraft(Draft draft, Runnable onDelete);
}
