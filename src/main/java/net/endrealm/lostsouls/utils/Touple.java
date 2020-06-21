package net.endrealm.lostsouls.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Touple<K, V, T> {
    private final K key;
    private V value;
    private T companion;

    public static <K,V, T> Touple<K,V,T> of(K key, V value, T companion) {
        return new Touple<>(key, value, companion);
    }
}
