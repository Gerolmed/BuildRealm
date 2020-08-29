package net.endrealm.buildrealm.export;

import net.endrealm.buildrealm.services.ThreadService;

import java.util.function.Consumer;

public final class ProcessBuilder<T> {
    private final Process<T> root;
    private final ThreadService threadService;
    private final Consumer<Process<T>> onBuild;

    private ProcessBuilder(ThreadService threadService, Consumer<Process<T>> onBuild) {
        this.threadService = threadService;
        this.onBuild = onBuild;

        //init root
        root = new Process<>() {
            @Override
            public void accept(T t) {
                runNext(t);
            }
        };
    }

    public ProcessBuilder<T> nextSync(Consumer<T> onValue) {
        root.append(new SyncProcess<>(threadService, true, onValue));
        return this;
    }

    public ProcessBuilder<T> nextAsync(Consumer<T> onValue) {
        root.append(new SyncProcess<>(threadService, false, onValue));
        return this;
    }

    public ProcessBuilder<T> next(Consumer<T> onValue) {
        root.append(new Process<>() {
            @Override
            public void accept(T t) {
                onValue.accept(t);
                runNext(t);
            }
        });
        return this;
    }

    public ProcessBuilder<T> nextRaw(Process<T> process) {
        root.append(process);
        return this;
    }

    public <K> ProcessBuilder<K> initSubChains(SubChainer<K, T> subChainer) {
        SubProcess<K, T> subProcess = new SubProcess<>(subChainer);
        root.append(subProcess);
        return new ProcessBuilder<>(threadService, subProcess::onSubChainBuilt);
    }

    public Process<T> build() {
        root.append(new ChainEnd<>());
        onBuild.accept(root);
        return root;
    }

    public static <T> ProcessBuilder<T> builder(ThreadService threadService, Class<T> tClass) {
        return new ProcessBuilder<>(threadService, tProcess -> {
        });
    }

    public interface SubChainer<K, T> {
        Iterable<K> findValues(T parent);
    }
}
