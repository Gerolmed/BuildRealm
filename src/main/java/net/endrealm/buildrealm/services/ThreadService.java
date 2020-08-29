package net.endrealm.buildrealm.services;

public interface ThreadService {
    void runAsync(Runnable runnable);

    void runSync(Runnable runnable);
}
