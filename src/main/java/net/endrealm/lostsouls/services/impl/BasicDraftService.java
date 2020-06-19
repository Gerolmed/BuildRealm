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
    public void accessibleDrafts(UUID playerId, Consumer<List<Draft>> onLoad) {
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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Draft> void replaceDraft(T oldDraft, Draft newDraft, Consumer<T> onSuccess) {
        if(blocked.contains(oldDraft.getId()) || oldDraft.isInvalid())
            return;
        if(blocked.contains(newDraft.getId()) || newDraft.isInvalid())
            return;
        blocked.add(oldDraft.getId());
        blocked.add(newDraft.getId());
        worldService.replace(oldDraft.getIdentity(), newDraft.getIdentity(), () -> {
            threadService.runAsync(() -> {
                T merge = (T) oldDraft.merge(newDraft);
                dataProvider.remove(newDraft);
                dataProvider.saveDraft(merge);
                blocked.remove(oldDraft.getId());
                blocked.remove(newDraft.getId());
                onSuccess.accept(merge);
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
            threadService.runAsync(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
        });
    }

    @Override
    public void publishAppend(Draft draft, Consumer<Piece> onFinish, Runnable onError) {
        ForkData forkData = draft.getForkData();
        if(forkData == null || blocked.contains(draft.getId())) {
            onError.run();
            return;
        }
        blocked.add(draft.getId());
        threadService.runAsync(() -> {
            Optional<Draft> parent = dataProvider.getDraft(forkData.getOriginId());
            if(parent.isEmpty()) {
                blocked.remove(draft.getId());
                onError.run();
                return;
            }
            String oldTarget = forkData.getOriginId();
            forkData.setOriginId(parent.get().getForkData().getOriginId());
            blocked.remove(draft.getId());
            publishFork(draft, onFinish, () -> {
                draft.getForkData().setOriginId(oldTarget);
                onError.run();
            });
        });
    }

    @Override
    public void publishFork(Draft draft, Consumer<Piece> onFinish, Runnable onError) {
        ForkData forkData = draft.getForkData();
        if(forkData == null || blocked.contains(draft.getId())) {
            onError.run();
            return;
        }

        blocked.add(draft.getId());
        threadService.runAsync(() -> {
            Optional<Draft> parentOpt = dataProvider.getDraft(forkData.getOriginId());
            if(parentOpt.isEmpty() || blocked.contains(parentOpt.get().getId())) {
                blocked.remove(draft.getId());
                onError.run();
                return;
            }
            Draft parentDraft = parentOpt.get();
            blocked.add(parentDraft.getId());

            if(!(parentDraft instanceof Piece)) {
                blocked.remove(draft.getId());
                blocked.remove(parentDraft.getId());
                onError.run();
                return;
            }
            Piece parent = (Piece) parentDraft;

            Piece piece = new Piece(
                    draft.getId(),
                    draft.getMembers().stream().map(member -> new Member(member.getUuid(), PermissionLevel.COLLABORATOR)).collect(Collectors.toList()),
                    draft.getNote(),
                    null, //clear any fork data if present
                    parent.getTheme(),
                    new Date(),
                    false);
            int pointer = parent.getForkCount()+1;
            piece.setNumber(pointer+"");
            piece.setPieceType(parent.getPieceType());
            parent.setForkCount(pointer);

            this.themeService.loadTheme(piece.getTheme(), theme -> {
                worldService.clone(draft.getIdentity(), piece.getIdentity(), () -> {
                    worldService.delete(draft.getIdentity(), () -> {

                        blocked.remove(draft.getId());
                        TypeCategory category = theme.getCategory(piece.getPieceType());
                        category.setPieceCount(category.getPieceCount()+1);

                        dataProvider.saveDraft(piece);
                        themeService.unlock(theme);
                        themeService.saveTheme(theme, () -> {
                            blocked.remove(piece.getId());
                            blocked.remove(parent.getId());
                            onFinish.accept(piece);
                        });

                    });
                });
            }, () -> {
                //Undo changes
                dataProvider.invalidate(piece);
                dataProvider.invalidate(parent);
                blocked.remove(piece.getId());
                blocked.remove(parent.getId());
            });
        });
    }

    @Override
    public void publishReplace(Draft draft, Consumer<Piece> onFinish, Runnable onError) {

        ForkData forkData = draft.getForkData();
        if(forkData == null || blocked.contains(draft.getId())) {
            onError.run();
            return;
        }
        Optional<Draft> parentOpt = dataProvider.getDraft(forkData.getOriginId());
        if(parentOpt.isEmpty() || blocked.contains(parentOpt.get().getId())) {
            blocked.remove(draft.getId());
            onError.run();
            return;
        }
        Draft parentDraft = parentOpt.get();
        blocked.add(parentDraft.getId());

        if(!(parentDraft instanceof Piece)) {
            blocked.remove(draft.getId());
            blocked.remove(parentDraft.getId());
            onError.run();
            return;
        }

        Piece parent = (Piece) parentDraft;

        replaceDraft(parent, draft, onFinish);
    }

    @Override
    public void deletePiece(Piece piece, Runnable onDelete) {
        if(blocked.contains(piece.getId()))
            return;
        if(piece.isInvalid()) {
            return;
        }
        blocked.add(piece.getId());
        themeService.loadTheme(
                piece.getTheme(), theme -> {
                    if(themeService.isLocked(theme)) {
                        blocked.remove(piece.getId());
                        return;
                    }
                    themeService.lock(theme);
                    dataProvider.remove(piece);
                    deleteDraft(piece, () -> {
                        TypeCategory category = theme.getCategory(piece.getPieceType());
                        category.setPieceCount(category.getPieceCount()-1);
                        themeService.unlock(theme);

                        themeService.saveTheme(theme, () -> {
                            blocked.remove(piece.getId());
                            onDelete.run();
                        });
                        //TODO: shift sub pieces
                    });

                }, () -> {
                    blocked.remove(piece.getId());
                    deleteDraft(piece, onDelete);
                    //TODO: shift sub pieces
                });
    }
}
