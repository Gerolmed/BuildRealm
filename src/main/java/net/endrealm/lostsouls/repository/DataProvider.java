package net.endrealm.lostsouls.repository;

import net.endrealm.lostsouls.data.entity.Draft;

import java.util.Optional;

public interface DataProvider {
    Optional<Draft> getDraft(String key);
    <T extends Draft> Optional<T> getDraftCast(String key);
    void saveDraft(Draft draft);
}
