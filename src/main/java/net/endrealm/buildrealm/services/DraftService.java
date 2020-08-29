package net.endrealm.buildrealm.services;

import net.endrealm.buildrealm.data.PieceType;
import net.endrealm.buildrealm.data.entity.Draft;
import net.endrealm.buildrealm.data.entity.Group;
import net.endrealm.buildrealm.data.entity.Piece;
import org.bukkit.World;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface DraftService {
    void loadDraft(String id, Consumer<Draft> onLoad, Runnable notExists);

    void accessibleDrafts(UUID playerId, Consumer<List<Draft>> onLoad);

    void createDraft(Consumer<Draft> onLoad, Runnable duplicateId);

    void saveDraft(Draft draft, Runnable onLoad);

    void unloadDraft(Draft draft, Runnable onFinish, Consumer<Exception> onFailure);

    void unloadDraft(String name, Runnable onFinish, Consumer<Exception> onFailure);

    /**
     * Generates a draft or finds currently generated draft
     */
    void generateDraft(Draft draft, Consumer<World> onGenerated, Consumer<Exception> onFailure);

    <T extends Draft> void replaceDraft(T oldDraft, Draft newDraft, Consumer<T> onSuccess);

    void deleteDraft(Draft draft, Runnable onDelete);

    void draftsByGroupAndType(String group, PieceType type, Consumer<List<Draft>> onLoad);

    void draftsByGroupAndType(Group group, PieceType type, Consumer<List<Draft>> onLoad);

    void publishNew(PieceType type, Group group, Draft draft, Consumer<Piece> onFinish, Runnable onError);

    void publishAppend(Draft draft, Consumer<Piece> onFinish, Runnable onError);

    void publishFork(Draft draft, Consumer<Piece> onFinish, Runnable onError);

    void publishReplace(Draft draft, Consumer<Piece> onFinish, Runnable onError);

    void deletePiece(Piece piece, Runnable onDelete);

    void createFork(Draft draft, UUID uniqueId, Consumer<Draft> onCreat);
}
