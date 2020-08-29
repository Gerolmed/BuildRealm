package net.endrealm.buildrealm.repository;

import net.endrealm.buildrealm.data.PieceType;
import net.endrealm.buildrealm.data.entity.Draft;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DraftRepository {
    Optional<Draft> findByKey(String key);

    List<Draft> findByMember(UUID uuid, boolean open, List<String> excludedDraftIds);

    void save(Draft value);

    String findFreeKey();

    void remove(Draft draft);

    List<Draft> findByGroupAndType(String group, PieceType type, List<String> exclude);

    List<Draft> findByParent(String parentId, List<String> exclude);
}
