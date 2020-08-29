package net.endrealm.buildrealm.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class Observable<T> {
    private T value;
    private final List<Consumer<T>> listeners;

    private Observable(T value) {
        this.value = value;
        this.listeners = new ArrayList<>();
    }

    public synchronized void next(T value) {
        this.value = value;
        listeners.forEach(tConsumer -> {
            try {
                tConsumer.accept(value);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public synchronized void subscribe(Consumer<T> onChange) {
        this.listeners.add(onChange);
        onChange.accept(value);
    }

    public synchronized Optional<T> get() {
        return Optional.ofNullable(value);
    }

    public <K> Observable<K> map(Mapper<T, K> mapper) {
        final Observable<K> mapped = Observable.of(null);
        get().ifPresent(t -> {
            mapped.next(mapper.map(t));
        });
        return mapped;
    }

    public static <T> Observable<T> of(T value) {
        return new Observable<>(value);
    }

    public interface Mapper<T, K> {
        K map(T val);
    }
}
