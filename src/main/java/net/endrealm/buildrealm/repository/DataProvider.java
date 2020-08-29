package net.endrealm.buildrealm.repository;

import net.endrealm.buildrealm.data.PieceType;
import net.endrealm.buildrealm.data.entity.Draft;
import net.endrealm.buildrealm.data.entity.Group;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DataProvider {
    Optional<Draft> getDraft(String key);

    List<Draft> getDraftsByUser(UUID uuid, boolean open);

    void saveDraft(Draft draft);

    String getFreeDraftId();

    void remove(Draft draft);

    Optional<Group> getGroup(String key);

    void saveGroup(Group group);

    List<Group> getAllGroups();

    void removeGroup(Group group);

    List<Draft> getDraftsByGroupAndType(String group, PieceType type);

    void invalidate(Draft draft);

    void validateCaches();

    List<Draft> getDraftsByParent(String parentId);
}
