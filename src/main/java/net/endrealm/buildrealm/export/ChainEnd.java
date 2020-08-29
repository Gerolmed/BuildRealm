package net.endrealm.buildrealm.export;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
class ChainEnd<T> extends Process<T> {
    private ChainEnd<T> chainEnd;

    @Override
    public void accept(T t) {
        if (chainEnd != null)
            chainEnd.accept(t);
    }

    @Override
    protected void runNext(T value) {
        accept(value);
    }

    @Override
    protected void append(Process<T> next) {
        if (next instanceof ChainEnd) {
            chainEnd = (ChainEnd<T>) next;
        }
    }

    @Override
    public void setNext(Process<T> next) {
        if (next instanceof ChainEnd) {
            chainEnd = (ChainEnd<T>) next;
        }
    }
}
