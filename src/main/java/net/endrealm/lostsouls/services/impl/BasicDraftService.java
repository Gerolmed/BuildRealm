package net.endrealm.lostsouls.services.impl;

import lombok.Data;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.exception.DuplicateKeyException;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThreadService;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Data
public class BasicDraftService implements DraftService {

    private final DataProvider dataProvider;
    private final ThreadService threadService;

    @Override
    public void loadDraft(String id, Consumer<Draft> onLoad, Runnable notExists) {
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

                    Draft draft = new Draft(dataProvider.getFreeDraftId(), new ArrayList<>(), "", null, null, new Date(), true);

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
        threadService.runAsync(
                () -> {
                    //TODO: save world if generated
                    dataProvider.saveDraft(draft);
                    postSave.run();
                }
        );
    }

    @Override
    public void generateDraft(Draft draft, Consumer<World> onGenerated, Consumer<Exception> onFailure) {

    }

    @Override
    public void unloadDraft(Draft draft, Runnable onFinish, Consumer<Exception> onFailure) {

    }

    @Override
    public void replaceDraft(Draft oldDraft, Draft newDraft, Runnable onSuccess) {
        threadService.runAsync(() -> {

            // TODO unload oldDraft world and newDraft world + remove newDraft world
            Draft merge = oldDraft.merge(newDraft);
            dataProvider.remove(newDraft);
            dataProvider.saveDraft(merge);
        });
    }
}
