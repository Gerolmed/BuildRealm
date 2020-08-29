package net.endrealm.buildrealm.repository.impl;

import lombok.Data;
import net.endrealm.buildrealm.data.PieceType;
import net.endrealm.buildrealm.data.entity.Draft;
import net.endrealm.buildrealm.data.entity.Group;
import net.endrealm.buildrealm.data.entity.Piece;
import net.endrealm.buildrealm.repository.Cache;
import net.endrealm.buildrealm.repository.DataProvider;
import net.endrealm.buildrealm.repository.DraftRepository;
import net.endrealm.buildrealm.repository.GroupRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("SimplifyOptionalCallChains")
@Data
public class BasicDataProvider implements DataProvider {

    private final Cache<Draft, String> draftCache;
    private final DraftRepository draftRepository;
    private final Cache<Group, String> groupCache;
    private final GroupRepository groupRepository;

    public void validateCaches() {
        groupCache.validateAll();
        draftCache.validateAll();
    }

    @Override
    public List<Draft> getDraftsByParent(String parentId) {
        List<Draft> drafts = draftCache.getAllBy(value -> value.getForkData() != null && value.getForkData().getOriginId().equals(parentId));

        List<Draft> newDrafts = draftRepository.findByParent(parentId, drafts.stream().map(Draft::getId).collect(Collectors.toList()));
        newDrafts.forEach(draft -> draftCache.add(draft.getId(), draft));
        drafts.addAll(newDrafts);
        return drafts;
    }

    @Override
    public synchronized Optional<Draft> getDraft(String key) {
        Optional<Draft> draftOpt = draftCache.get(key);
        if (!draftOpt.isPresent()) {
            draftOpt = draftRepository.findByKey(key);
            draftOpt.ifPresent(draft -> draftCache.add(draft.getId(), draft));
        }
        return draftOpt;
    }

    @Override
    public List<Draft> getDraftsByUser(UUID uuid, boolean open) {
        List<Draft> drafts = draftCache.getAllBy(value -> value.isOpen() == open && value.hasMember(uuid));

        List<Draft> newDrafts = draftRepository.findByMember(uuid, open, drafts.stream().map(Draft::getId).collect(Collectors.toList()));
        newDrafts.forEach(draft -> draftCache.add(draft.getId(), draft));
        drafts.addAll(newDrafts);
        return drafts;
    }

    @Override
    public synchronized void saveDraft(Draft draft) {
        draftCache.add(draft.getId(), draft);
        draftRepository.save(draft);
    }

    @Override
    public synchronized String getFreeDraftId() {
        return draftRepository.findFreeKey();
    }

    @Override
    public void remove(Draft draft) {
        draftCache.markDirty(draft.getId());
        draftRepository.remove(draft);
    }

    @Override
    public Optional<Group> getGroup(String key) {
        Optional<Group> draftOpt = groupCache.get(key);
        if (!draftOpt.isPresent()) {
            draftOpt = groupRepository.get(key);
            draftOpt.ifPresent(draft -> groupCache.add(draft.getName(), draft));
        }
        return draftOpt;
    }

    @Override
    public void saveGroup(Group group) {
        groupCache.add(group.getName(), group);
        groupRepository.save(group);
    }

    @Override
    public List<Group> getAllGroups() {
        List<Group> cachedGroups = groupCache.getAllBy(value -> true);
        List<Group> newGroups = groupRepository.getAll(cachedGroups.stream().map(Group::getName).collect(Collectors.toList()));
        newGroups.forEach(group -> groupCache.add(group.getName(), group));
        cachedGroups.addAll(newGroups);
        return cachedGroups;
    }

    @Override
    public void removeGroup(Group group) {
        groupCache.markDirty(group.getName());
        groupRepository.delete(group.getName());
    }

    @Override
    public List<Draft> getDraftsByGroupAndType(String group, PieceType type) {
        List<Draft> drafts = draftCache.getAllBy(value -> {
            if (!(value instanceof Piece)) return false;
            Piece piece = (Piece) value;

            return piece.getGroup().equals(group) && piece.getPieceType() == type;
        });

        List<Draft> newDrafts = draftRepository.findByGroupAndType(group, type, drafts.stream().map(Draft::getId).collect(Collectors.toList()));
        newDrafts.forEach(draft -> draftCache.add(draft.getId(), draft));
        drafts.addAll(newDrafts);
        return drafts;
    }

    @Override
    public void invalidate(Draft draft) {
        draftCache.markDirty(draft.getId());
    }
}
