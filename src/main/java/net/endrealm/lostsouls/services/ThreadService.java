package net.endrealm.lostsouls.services;

public interface ThreadService {
    void runAsync(Runnable runnable);
    void runSync(Runnable runnable);
}
