package net.endrealm.buildrealm.export;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Iterator;

@EqualsAndHashCode(callSuper = true)
@Data
class SubProcess<K, T> extends Process<T> {


    private final ProcessBuilder.SubChainer<K, T> subChainer;
    private Process<K> subProcess;

    public SubProcess(ProcessBuilder.SubChainer<K, T> subChainer) {
        this.subChainer = subChainer;
    }

    void onSubChainBuilt(Process<K> process) {
        this.subProcess = process;
        process.append(new ChainEnd<>() {
            @Override
            public void accept(K k) {
                next();
            }
        });
    }

    private void next() {
        if (iterator.hasNext()) {
            subProcess.accept(iterator.next());
            return;
        }
        runNext(old);
    }

    //This will cause issues if trying to run processes parallel!
    private Iterator<K> iterator;

    private T old;

    @Override
    public void accept(T t) {
        old = t;
        iterator = subChainer.findValues(t).iterator();
        next();
    }
}
