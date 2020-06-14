package net.endrealm.lostsouls.repository.impl;

import lombok.Data;
import net.endrealm.lostsouls.repository.Cache;
import net.endrealm.lostsouls.utils.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class BasicCache<T, K> implements Cache<T, K> {
    private final Map<K, Pair<T, Long>> cacheMap = new HashMap<>();

    private final Long cacheDuration;

    @Override
    public Optional<T> get(K key) {
        Pair<T, Long> entry = cacheMap.get(key);

        if(entry == null) {
            return Optional.empty();
        }

        Long now = System.currentTimeMillis();

        if(entry.getValue() > now) {
            markDirty(key);
            return Optional.empty();
        }

        entry.setValue(now + cacheDuration);

        return Optional.of(entry.getKey());
    }

    @Override
    public void add(K key, T value) {
        cacheMap.put(key, Pair.of(value, System.currentTimeMillis() + cacheDuration));
    }

    @Override
    public Optional<T> markDirty(K key) {
        Pair<T, Long> pair = cacheMap.remove(key);
        return pair != null ? Optional.of(pair.getKey()) : Optional.empty();
    }
}
