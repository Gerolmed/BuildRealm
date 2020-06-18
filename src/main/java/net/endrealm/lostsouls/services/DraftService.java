package net.endrealm.lostsouls.services;

import net.endrealm.lostsouls.data.PieceType;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.data.entity.Piece;
import net.endrealm.lostsouls.data.entity.Theme;
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
    void unloadDraft(String name, Runnable onFinish, Consumer<Exception> onFailure);
    /**
     * Generates a draft or finds currently generated draft
     *
     */
    void generateDraft(Draft draft, Consumer<World> onGenerated, Consumer<Exception> onFailure);
    <T extends Draft> void replaceDraft(T oldDraft, Draft newDraft, Consumer<T> onSuccess);

    void deleteDraft(Draft draft, Runnable onDelete);
    void draftsByThemeAndType(String theme, PieceType type, Consumer<List<Draft>> onLoad);
    void draftsByThemeAndType(Theme theme, PieceType type, Consumer<List<Draft>> onLoad);

    void publishNew(PieceType type, Theme theme, Draft draft, Consumer<Piece> onFinish, Runnable onError);
    void publishAppend(Draft draft, Consumer<Piece> onFinish, Runnable onError);
    void publishFork(Draft draft, Consumer<Piece> onFinish, Runnable onError);
    void publishReplace(Draft draft, Consumer<Piece> onFinish, Runnable onError);

}
