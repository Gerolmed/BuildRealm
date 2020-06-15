package net.endrealm.lostsouls.repository.impl;

import lombok.Data;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.repository.Cache;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.repository.DraftRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class BasicDataProvider implements DataProvider {

    private final Cache<Draft, String> draftCache;
    private final DraftRepository draftRepository;

    @Override
    public Optional<Draft> getDraft(String key) {
        Optional<Draft> draftOpt = draftCache.get(key);
        if(!draftOpt.isPresent()) {
            draftOpt = draftRepository.findByKey(key);
            draftOpt.ifPresent(draft -> draftCache.add(draft.getId(), draft));
        }
        return draftOpt;
    }

    @Override
    public List<Draft> getDraftsByUser(UUID uuid) {
        List<Draft> drafts = draftCache.getAllBy(value -> value.hasMember(uuid));

        List<Draft> newDrafts = draftRepository.findByMember(uuid, true, drafts.stream().map(Draft::getId).collect(Collectors.toList()));
        newDrafts.forEach(draft -> draftCache.add(draft.getId(), draft));
        drafts.addAll(newDrafts);
        return drafts;
    }

    @Override
    public <T extends Draft> Optional<T> getDraftCast(String key) {
        //noinspection unchecked
        return getDraft(key).map(t -> (T) t);
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
}
