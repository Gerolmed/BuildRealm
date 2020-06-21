package net.endrealm.lostsouls.export;

import net.endrealm.lostsouls.services.ThreadService;

import java.util.function.Consumer;

public class SyncProcess<T> extends Process<T> {
    private final ThreadService threadService;
    private final boolean isSynced;
    private final Consumer<T> onValue;

    public SyncProcess(ThreadService threadService, boolean isSynced, Consumer<T> onValue) {
        this.threadService = threadService;
        this.isSynced = isSynced;
        this.onValue = onValue;
    }

    @Override
    public void accept(T t) {
        run(()-> {
            onValue.accept(t);
            threadService.runAsync(this::runNext);
        });
    }

    private void run(Runnable runnable) {
        if(isSynced) {
            threadService.runSync(runnable);
        } else {
            threadService.runAsync(runnable);
        }
    }
}
