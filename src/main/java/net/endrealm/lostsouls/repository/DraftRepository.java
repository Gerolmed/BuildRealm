package net.endrealm.lostsouls.repository;

import net.endrealm.lostsouls.data.entity.Draft;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DraftRepository {
    Optional<Draft> findByKey(String key);
    List<Draft> findByMember(UUID uuid, boolean open, List<String> excludedDraftIds);
    void save(Draft value);
    String findFreeKey();

    void remove(Draft draft);
}
