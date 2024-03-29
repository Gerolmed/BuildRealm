package net.endrealm.buildrealm.repository.impl;

import lombok.Data;
import net.endrealm.buildrealm.repository.Cache;
import net.endrealm.buildrealm.utils.Invalidateble;
import net.endrealm.buildrealm.utils.Pair;

import java.util.*;

@Data
public class BasicCache<T extends Invalidateble, K> implements Cache<T, K> {
    private final Map<K, Pair<T, Long>> cacheMap = new HashMap<>();

    private final Long cacheDuration;

    @Override
    public synchronized Optional<T> get(K key) {
        Pair<T, Long> entry = cacheMap.get(key);

        if (entry == null) {
            return Optional.empty();
        }


        if (!validateOrRefresh(key, entry)) {
            return Optional.empty();
        }

        return Optional.of(entry.getKey());
    }

    private synchronized boolean validateOrRefresh(K key, Pair<T, Long> entry) {
        long now = System.currentTimeMillis();

        if (entry.getValue() < now || entry.getKey().isInvalid()) {
            markDirty(key);
            return false;
        }
        entry.setValue(now + cacheDuration);
        return true;
    }

    @Override
    public synchronized List<T> getAllBy(Filter<T> matches) {
        List<T> found = new ArrayList<>();
        new ArrayList<>(cacheMap.entrySet()).forEach((k) -> {
            if (matches.matches(k.getValue().getKey()) && !k.getValue().getKey().isInvalid())
                found.add(k.getValue().getKey());
        });

        return found;
    }

    @Override
    public synchronized void add(K key, T value) {
        if (!value.equals(cacheMap.getOrDefault(key, Pair.of(null, null)).getKey()))
            markDirty(key);
        cacheMap.put(key, Pair.of(value, System.currentTimeMillis() + cacheDuration));
    }

    @Override
    public synchronized Optional<T> markDirty(K key) {
        Pair<T, Long> pair = cacheMap.remove(key);
        if (pair != null) {
            pair.getKey().setInvalid(true);
            return Optional.of(pair.getKey());
        }
        return Optional.empty();
    }

    public synchronized void validateAll() {
        new ArrayList<>(cacheMap.entrySet()).forEach(kPairEntry -> validateOrRefresh(kPairEntry.getKey(), kPairEntry.getValue()));
    }
}
