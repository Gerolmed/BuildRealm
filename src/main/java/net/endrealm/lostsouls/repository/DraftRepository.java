package net.endrealm.lostsouls.repository;

import net.endrealm.lostsouls.data.entity.Draft;

import java.util.Optional;

public interface DraftRepository {
    Optional<Draft> findByKey(String key);
    void save(Draft value);
}
