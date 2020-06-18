package net.endrealm.lostsouls.services.impl;

import lombok.Data;
import net.endrealm.lostsouls.data.PermissionLevel;
import net.endrealm.lostsouls.data.PieceType;
import net.endrealm.lostsouls.data.entity.*;
import net.endrealm.lostsouls.exception.DuplicateKeyException;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.services.DraftService;
import net.endrealm.lostsouls.services.ThemeService;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.world.WorldIdentity;
import net.endrealm.lostsouls.world.WorldService;
import org.bukkit.World;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Data
public class BasicDraftService implements DraftService {

    private final DataProvider dataProvider;
    private final ThreadService threadService;
    private final WorldService worldService;
    private final ThemeService themeService;

    private final Set<String> blocked = new HashSet<>();


    @Override
    public void loadDraft(String id, Consumer<Draft> onLoad, Runnable notExists) {
        if(blocked.contains(id)) return; //TODO call error
        threadService.runAsync(() -> dataProvider.getDraft(id).ifPresentOrElse(onLoad, notExists));
    }

    @Override
    public void ownedDrafts(UUID playerId, Consumer<List<Draft>> onLoad) {
        threadService.runAsync(
                () -> onLoad.accept(dataProvider.getDraftsByUser(playerId, true))
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
        if(blocked.contains(draft.getId())) {
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
        if(blocked.contains(draft.getId())) {
            onFailure.accept(new Exception("World is being blocked"));
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
        if(blocked.contains(name)){
            onFailure.accept(new Exception("World is being blocked"));
            return;
        }
        blocked.add(name);
        worldService.unload(new WorldIdentity(name, false), () -> {
            blocked.remove(name);
            onFinish.run();
        });
    }

    @Override
    public void replaceDraft(Draft oldDraft, Draft newDraft, Runnable onSuccess) {
        if(blocked.contains(oldDraft.getId()) || oldDraft.isInvalid())
            return;
        if(blocked.contains(newDraft.getId()) || newDraft.isInvalid())
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
        if(blocked.contains(draft.getId()))
            return;
        if(draft.isInvalid()) {
            return;
        }
        blocked.add(draft.getId());
        threadService.runAsync(() -> {
            dataProvider.remove(draft);
            worldService.delete(draft.getIdentity(), () -> {
                blocked.remove(draft.getId());
                onDelete.run();
            });
        });
    }

    @Override
    public void draftsByThemeAndType(String theme, PieceType type, Consumer<List<Draft>> onLoad) {
        threadService.runAsync(() -> {
            List<Draft> drafts = dataProvider.getDraftsByThemeAndType(theme, type);
            onLoad.accept(drafts);
        });
    }

    @Override
    public void draftsByThemeAndType(Theme theme, PieceType type, Consumer<List<Draft>> onLoad) {
        draftsByThemeAndType(theme.getName(), type, onLoad);
    }

    @Override
    public void publishNew(PieceType type, Theme theme, Draft draft, Consumer<Piece> onFinish, Runnable onError) {
        if(theme.isStale() || theme.isInvalid() || draft.isInvalid() || !draft.isOpen() || blocked.contains(draft.getId()) || themeService.isLocked(theme)) {
            onError.run();
            return;
        }
        themeService.lock(theme);
        blocked.add(draft.getId());
        Piece piece = new Piece(
                draft.getId(),
                draft.getMembers().stream().map(member -> new Member(member.getUuid(), PermissionLevel.COLLABORATOR)).collect(Collectors.toList()),
                draft.getNote(),
                null, //clear any fork data if present
                theme.getName(),
                new Date(),
                false);
        piece.setPieceType(type);
        worldService.clone(draft.getIdentity(), piece.getIdentity(), () -> {
            worldService.delete(draft.getIdentity(), () -> {

                blocked.remove(draft.getId());
                TypeCategory category = theme.getCategory(type);
                int pointer = category.getMainPointer();
                piece.setNumber(pointer+"");
                category.setMainPointer(pointer+1);
                category.setPieceCount(category.getPieceCount()+1);

                dataProvider.saveDraft(piece);
                themeService.unlock(theme);
                themeService.saveTheme(theme, () -> {
                    blocked.remove(piece.getId());
                    onFinish.accept(piece);
                });

            });
        });
    }
}
