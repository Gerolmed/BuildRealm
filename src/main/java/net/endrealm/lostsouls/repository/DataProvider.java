package net.endrealm.lostsouls.repository;

import net.endrealm.lostsouls.data.entity.Draft;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DataProvider {
    Optional<Draft> getDraft(String key);
    List<Draft> getDraftsByUser(UUID uuid);
    <T extends Draft> Optional<T> getDraftCast(String key);
    void saveDraft(Draft draft);

    String getFreeDraftId();

    void remove(Draft draft);
}
