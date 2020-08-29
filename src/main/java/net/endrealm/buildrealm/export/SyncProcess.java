package net.endrealm.buildrealm.export;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.endrealm.buildrealm.services.ThreadService;

import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@Data
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
        run(() -> {
            onValue.accept(t);
            threadService.runAsync(() -> {
                runNext(t);
            });
        });
    }

    private void run(Runnable runnable) {
        if (isSynced) {
            threadService.runSync(runnable);
        } else {
            threadService.runAsync(runnable);
        }
    }
}
