package net.endrealm.lostsouls.repository;

import java.util.Optional;

public interface Cache<T, K> {
    Optional<T> get(K key);
    void add(K key, T value);
    Optional<T> markDirty(K key);
}
