package net.endrealm.buildrealm.services.impl;

import lombok.Data;
import net.endrealm.buildrealm.data.PermissionLevel;
import net.endrealm.buildrealm.data.PieceType;
import net.endrealm.buildrealm.data.entity.*;
import net.endrealm.buildrealm.exception.DuplicateKeyException;
import net.endrealm.buildrealm.repository.DataProvider;
import net.endrealm.buildrealm.services.DraftService;
import net.endrealm.buildrealm.services.GroupService;
import net.endrealm.buildrealm.services.ThreadService;
import net.endrealm.buildrealm.world.WorldIdentity;
import net.endrealm.buildrealm.world.WorldService;
import org.bukkit.World;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Data
public class BasicDraftService implements DraftService {

    private final DataProvider dataProvider;
    private final ThreadService threadService;
    private final WorldService worldService;
    private final GroupService groupService;

    private final Set<String> blocked = new HashSet<>();


    @Override
    public void loadDraft(String id, Consumer<Draft> onLoad, Runnable notExists) {
        if (blocked.contains(id)) return;
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
        if (blocked.contains(draft.getId())) {
            return;
        }
        if (draft.isInvalid()) {
            return;
        }
        threadService.runSync(
                () -> {
                    worldService.save(draft.getIdentity());
                    threadService.runAsync(() -> {
                        dataProvider.saveDraft(draft);
                        postSave.run();
                    });
                }
        );
    }

    @Override
    public void generateDraft(Draft draft, Consumer<World> onGenerated, Consumer<Exception> onFailure) {
        if (blocked.contains(draft.getId())) {
            onFailure.accept(new Exception("World is being blocked"));
            return;
        }
        if (draft.isInvalid()) {
            onFailure.accept(new Exception("Draft is invalid"));
            return;
        }
        worldService.generate(draft.getIdentity(), world -> {
            if (world == null)
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
        if (blocked.contains(name)) {
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
        if (blocked.contains(oldDraft.getId()) || oldDraft.isInvalid())
            return;
        if (blocked.contains(newDraft.getId()) || newDraft.isInvalid())
            return;
        blocked.add(oldDraft.getId());
        blocked.add(newDraft.getId());
        worldService.replace(oldDraft.getIdentity(), newDraft.getIdentity(), () -> {
            threadService.runAsync(() -> {
                T merge = (T) oldDraft.merge(newDraft);
                merge.setLastUpdated(new Date());
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
        if (blocked.contains(draft.getId()))
            return;
        if (draft.isInvalid()) {
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
    public void draftsByGroupAndType(String group, PieceType type, Consumer<List<Draft>> onLoad) {
        threadService.runAsync(() -> {
            List<Draft> drafts = dataProvider.getDraftsByGroupAndType(group, type);
            onLoad.accept(drafts);
        });
    }

    @Override
    public void draftsByGroupAndType(Group group, PieceType type, Consumer<List<Draft>> onLoad) {
        draftsByGroupAndType(group.getName(), type, onLoad);
    }

    @Override
    public void publishNew(PieceType type, Group group, Draft draft, Consumer<Piece> onFinish, Runnable onError) {
        if (group.isStale() || group.isInvalid() || draft.isInvalid() || !draft.isOpen() || blocked.contains(draft.getId()) || groupService.isLocked(group)) {
            onError.run();
            return;
        }
        groupService.lock(group);
        blocked.add(draft.getId());
        Piece piece = new Piece(
                draft.getId(),
                draft.getMembers().stream().map(member -> new Member(member.getUuid(), PermissionLevel.COLLABORATOR)).collect(Collectors.toList()),
                draft.getNote(),
                null, //clear any fork data if present
                group.getName(),
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
                    TypeCategory category = group.getCategory(type);
                    int pointer = category.getMainPointer();
                    piece.setNumber(pointer + "");
                    category.setMainPointer(pointer + 1);
                    category.setPieceCount(category.getPieceCount() + 1);

                    dataProvider.saveDraft(piece);
                    groupService.unlock(group);
                    groupService.saveGroup(group, () -> {
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
        if (forkData == null || blocked.contains(draft.getId())) {
            onError.run();
            return;
        }
        blocked.add(draft.getId());
        threadService.runAsync(() -> {
            Optional<Draft> parent = dataProvider.getDraft(forkData.getOriginId());
            if (parent.isEmpty()) {
                blocked.remove(draft.getId());
                onError.run();
                return;
            }
            Draft parentDraft = parent.get();
            String oldTarget = forkData.getOriginId();
            draft.setForkData(parentDraft.getForkData());
            if (draft.getForkData() != null) {
                blocked.remove(draft.getId());
                publishFork(draft, onFinish, () -> {
                    draft.getForkData().setOriginId(oldTarget);
                    onError.run();
                });
            } else {
                if (!(parentDraft instanceof Piece)) {
                    onError.run();
                    return;
                }
                Piece parentPiece = (Piece) parentDraft;
                groupService.loadGroup(parentPiece.getGroup(), group -> {
                    blocked.remove(draft.getId());
                    publishNew(parentPiece.getPieceType(), group, draft, onFinish, () -> {
                        if (draft.getForkData() == null)
                            draft.setForkData(new ForkData(oldTarget));
                        onError.run();
                    });
                }, () -> {
                    blocked.remove(draft.getId());
                    if (draft.getForkData() == null)
                        draft.setForkData(new ForkData(oldTarget));
                    onError.run();
                });
            }
        });
    }

    @Override
    public void publishFork(Draft draft, Consumer<Piece> onFinish, Runnable onError) {
        ForkData forkData = draft.getForkData();
        if (forkData == null || blocked.contains(draft.getId())) {
            onError.run();
            return;
        }

        blocked.add(draft.getId());
        threadService.runAsync(() -> {
            Optional<Draft> parentOpt = dataProvider.getDraft(forkData.getOriginId());
            if (parentOpt.isEmpty() || blocked.contains(parentOpt.get().getId())) {
                blocked.remove(draft.getId());
                onError.run();
                return;
            }
            Draft parentDraft = parentOpt.get();
            blocked.add(parentDraft.getId());

            if (!(parentDraft instanceof Piece)) {
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
                    new ForkData(parent.getId()),
                    parent.getGroup(),
                    new Date(),
                    false);
            int pointer = parent.getForkCount() + 1;
            piece.setNumber(pointer + "");
            piece.setPieceType(parent.getPieceType());

            parent.setForkCount(pointer);

            this.groupService.loadGroup(piece.getGroup(), group -> {
                worldService.clone(draft.getIdentity(), piece.getIdentity(), () -> {
                    worldService.delete(draft.getIdentity(), () -> {

                        threadService.runAsync(() -> {
                            dataProvider.saveDraft(parent);
                            dataProvider.saveDraft(piece);
                            blocked.remove(parent.getId());
                            blocked.remove(draft.getId());

                            TypeCategory category = group.getCategory(piece.getPieceType());
                            category.setPieceCount(category.getPieceCount() + 1);

                            groupService.unlock(group);
                            groupService.saveGroup(group, () -> {
                                onFinish.accept(piece);
                            });
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
        if (forkData == null || blocked.contains(draft.getId())) {
            onError.run();
            return;
        }
        blocked.add(draft.getId());

        Optional<Draft> parentOpt = dataProvider.getDraft(forkData.getOriginId());
        if (parentOpt.isEmpty() || blocked.contains(parentOpt.get().getId())) {
            blocked.remove(draft.getId());
            onError.run();
            return;
        }
        Draft parentDraft = parentOpt.get();
        blocked.add(parentDraft.getId());

        if (!(parentDraft instanceof Piece)) {
            blocked.remove(draft.getId());
            blocked.remove(parentDraft.getId());
            onError.run();
            return;
        }

        Piece parent = (Piece) parentDraft;
        blocked.remove(draft.getId());
        blocked.remove(parentDraft.getId());
        replaceDraft(parent, draft, onFinish);
    }

    @Override
    public void deletePiece(Piece piece, Runnable onDelete) {
        if (blocked.contains(piece.getId()))
            return;
        if (piece.isInvalid()) {
            return;
        }
        blocked.add(piece.getId());
        groupService.loadGroup(
                piece.getGroup(), group -> {
                    if (groupService.isLocked(group)) {
                        blocked.remove(piece.getId());
                        return;
                    }
                    groupService.lock(group);
                    blocked.remove(piece.getId());

                    deleteDraft(piece, () -> {
                        TypeCategory category = group.getCategory(piece.getPieceType());
                        category.setPieceCount(category.getPieceCount() - 1);
                        groupService.unlock(group);

                        groupService.saveGroup(group, () -> {
                            shiftSubPieces(piece, onDelete);

                        });
                    });

                }, () -> {
                    blocked.remove(piece.getId());
                    deleteDraft(piece, () -> {
                        shiftSubPieces(piece, onDelete);
                    });
                });
    }

    @SuppressWarnings("BusyWait")
    private void shiftSubPieces(Piece piece, Runnable onComplete) {
        List<Draft> drafts = dataProvider.getDraftsByParent(piece.getId());
        drafts.forEach(draft -> {
            while (blocked.contains(draft.getId())) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            blocked.add(draft.getId());
        });
        foreachValidDraft(drafts.iterator(), draft -> {
            blocked.remove(draft.getId());
            if (draft instanceof Piece) {
                Piece subPiece = (Piece) draft;
                subPiece.setNumber(piece.getNumber() + "_" + subPiece.getNumber());
            }
            draft.setForkData(piece.getForkData());
            saveDraft(draft, () -> {
            });
        }, draft -> {
            blocked.remove(draft.getId());
        }, onComplete);

    }

    private void foreachValidDraft(Iterator<Draft> drafts, Consumer<Draft> onDraft, Consumer<Draft> onRemoved, Runnable onComplete) {
        if (!drafts.hasNext()) {
            onComplete.run();
            return;
        }
        Draft draft = drafts.next();
        if (draft.isInvalid()) {
            Optional<Draft> optionalDraft = dataProvider.getDraft(draft.getId());
            if (optionalDraft.isEmpty()) {
                onRemoved.accept(draft);
                foreachValidDraft(drafts, onDraft, onRemoved, onComplete);
                return;
            }

            draft = optionalDraft.get();
        }

        onDraft.accept(draft);
        foreachValidDraft(drafts, onDraft, onRemoved, onComplete);
    }

    @Override
    public void createFork(Draft draft, UUID uniqueId, Consumer<Draft> onCreate) {
        if (blocked.contains(draft.getId())) {
            return;
        }
        blocked.add(draft.getId());

        createDraft(newDraft -> {
            blocked.add(newDraft.getId());
            newDraft.getMembers().add(new Member(uniqueId, PermissionLevel.OWNER));
            newDraft.setForkData(new ForkData(draft.getId()));
            dataProvider.saveDraft(newDraft);
            worldService.clone(draft.getIdentity(), newDraft.getIdentity(), () -> {
                blocked.remove(draft.getId());
                blocked.remove(newDraft.getId());
                onCreate.accept(newDraft);
            });
        }, () -> {
        });
    }
}
