package net.endrealm.lostsouls.export;

import lombok.Data;

import java.util.function.Consumer;

@Data
public abstract class Process<T> implements Consumer<T> {
    private Process<T> next;
    protected void append(Process<T> newProcess) {
        if(next == null)
            next = newProcess;
        else
            next.append(newProcess);
    }

    protected void runNext() {
        next.runNext();
    }
}
