package net.endrealm.lostsouls.repository.impl;

import lombok.Data;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.repository.Cache;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.repository.DraftRepository;

import java.util.Optional;

@Data
public class BasicDataProvider implements DataProvider {

    private final Cache<Draft, String> draftCache;
    private final DraftRepository draftRepository;

    @Override
    public Optional<Draft> getDraft(String key) {
        Optional<Draft> draftOpt = draftCache.get(key);
        if(!draftOpt.isPresent()) {
            draftOpt = draftRepository.findByKey(key);
        }
        return draftOpt;
    }

    @Override
    public <T extends Draft> Optional<T> getDraftCast(String key) {
        //noinspection unchecked
        return getDraft(key).map(t -> (T) t);
    }

    @Override
    public void saveDraft(Draft draft) {
        draftCache.add(draft.getId(), draft);
        draftRepository.save(draft);
    }
}
