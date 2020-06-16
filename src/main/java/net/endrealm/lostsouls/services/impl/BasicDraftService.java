package net.endrealm.lostsouls.services.impl;

import lombok.Data;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.exception.DuplicateKeyException;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.world.WorldIdentity;
import net.endrealm.lostsouls.world.WorldService;
import org.bukkit.World;

import java.util.*;
import java.util.function.Consumer;

@Data
public class BasicDraftService implements DraftService {

    private final DataProvider dataProvider;
    private final ThreadService threadService;
    private final WorldService worldService;

    private final Set<String> deletedQueue = new HashSet<>();
    private final Set<String> unloadingQueue = Collections.synchronizedSet(new HashSet<>());


    @Override
    public void loadDraft(String id, Consumer<Draft> onLoad, Runnable notExists) {
        if(unloadingQueue.contains(id)) return; //TODO call error
        threadService.runAsync(() -> dataProvider.getDraft(id).ifPresentOrElse(onLoad, notExists));
    }

    @Override
    public void ownedDrafts(UUID playerId, Consumer<List<Draft>> onLoad) {
        threadService.runAsync(
                () -> onLoad.accept(dataProvider.getDraftsByUser(playerId))
        );
    }

    @Override
    public void createDraft(Consumer<Draft> onLoad, Runnable duplicateId) {
        threadService.runAsync(
                () -> {

                    Draft draft = new Draft(dataProvider.getFreeDraftId(), new ArrayList<>(), "", null, null, new Date(), true, false);

                    try {
                        dataProvider.saveDraft(draft);
                    } catch (DuplicateKeyException e) {
                        duplicateId.run();
                        return;
                    }
                    onLoad.accept(draft);
                }
        );
    }

    @Override
    public void saveDraft(Draft draft, Runnable postSave) {
        if(isInDeletionQueue(draft.getId())) {
            return;
        }
        if(draft.isInvalid()) {
            return;
        }
        threadService.runAsync(
                () -> {
                    //TODO: save world if generated
                    worldService.save(draft.getIdentity());
                    dataProvider.saveDraft(draft);
                    postSave.run();
                }
        );
    }

    @Override
    public void generateDraft(Draft draft, Consumer<World> onGenerated, Consumer<Exception> onFailure) {
        if(isInDeletionQueue(draft.getId())) {
            onFailure.accept(new Exception("World is being deleted"));
            return;
        }
        if(draft.isInvalid()) {
            onFailure.accept(new Exception("Draft is invalid"));
            return;
        }
        worldService.generate(draft.getIdentity(), world -> {
            if(world == null)
                onFailure.accept(null);
            else
                onGenerated.accept(world);
        });
    }

    @Override
    public void unloadDraft(Draft draft, Runnable onFinish, Consumer<Exception> onFailure) {

    }

    @Override
    public void unloadDraft(String name, Runnable onFinish, Consumer<Exception> onFailure) {
        if(unloadingQueue.contains(name)){
            onFailure.accept(new Exception("World is being unloaded"));
            return;
        }
        if(isInDeletionQueue(name)) {
            onFailure.accept(new Exception("World is being deleted"));
            return;
        }
        unloadingQueue.add(name);
        worldService.unload(new WorldIdentity(name, false), () -> {
            unloadingQueue.remove(name);
            onFinish.run();
        });
    }

    @Override
    public void replaceDraft(Draft oldDraft, Draft newDraft, Runnable onSuccess) {
        if(isInDeletionQueue(oldDraft.getId()) || oldDraft.isInvalid())
            return;
        if(isInDeletionQueue(newDraft.getId()) || newDraft.isInvalid())
            return;

        worldService.replace(oldDraft.getIdentity(), newDraft.getIdentity(), () -> {
            threadService.runAsync(() -> {
                Draft merge = oldDraft.merge(newDraft);
                dataProvider.remove(newDraft);
                dataProvider.saveDraft(merge);
            });
        });
    }

    @Override
    public void deleteDraft(Draft draft, Runnable onDelete) {
        if(isInDeletionQueue(draft.getId()))
            return;
        if(draft.isInvalid()) {
            return;
        }
        addToDeletionQueue(draft.getId());
        threadService.runAsync(() -> {
            dataProvider.remove(draft);
            worldService.delete(draft.getIdentity(), () -> {
                removeFromDeletionQueue(draft.getId());
                onDelete.run();
            });
        });
    }

    private synchronized void addToDeletionQueue(String id) {
        deletedQueue.add(id);
    }
    private synchronized boolean isInDeletionQueue(String id) {
        return deletedQueue.contains(id);
    }
    private synchronized void removeFromDeletionQueue(String id) {
        deletedQueue.remove(id);
    }
}
