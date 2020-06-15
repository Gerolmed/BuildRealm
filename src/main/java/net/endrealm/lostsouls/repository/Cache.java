package net.endrealm.lostsouls.repository;

import java.util.List;
import java.util.Optional;

public interface Cache<T, K> {
    Optional<T> get(K key);
    List<T> getAllBy(Filter<T> matches);
    void add(K key, T value);
    Optional<T> markDirty(K key);

    @FunctionalInterface
    interface Filter<T> {
        boolean matches(T value);
    }
}
