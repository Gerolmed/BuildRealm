package net.endrealm.buildrealm.repository;

import net.endrealm.buildrealm.utils.Invalidateble;

import java.util.List;
import java.util.Optional;

public interface Cache<T extends Invalidateble, K> {
    Optional<T> get(K key);

    List<T> getAllBy(Filter<T> matches);

    void add(K key, T value);

    Optional<T> markDirty(K key);

    void validateAll();

    @FunctionalInterface
    interface Filter<T> {
        boolean matches(T value);
    }
}
