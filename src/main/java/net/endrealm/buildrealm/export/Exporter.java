package net.endrealm.buildrealm.export;

import net.endrealm.buildrealm.data.entity.Group;
import net.endrealm.buildrealm.repository.DataProvider;
import net.endrealm.buildrealm.services.ThreadService;
import net.endrealm.buildrealm.world.WorldService;

import java.io.File;

public final class Exporter implements Runnable {
    private final Group group;
    private final Process<Group> process;

    public Exporter(File dataFolder, Group group, DataProvider dataProvider, ThreadService threadService, WorldService worldService, Runnable onFinish) {
        this.group = group;
        this.process = ExportUtils.createExportChain(dataFolder, threadService, dataProvider, worldService, onFinish);
    }

    @Override
    public void run() {
        process.accept(group);
    }
}
