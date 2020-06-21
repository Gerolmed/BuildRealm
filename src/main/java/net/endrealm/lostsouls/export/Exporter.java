package net.endrealm.lostsouls.export;

import net.endrealm.lostsouls.data.entity.Theme;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.world.WorldService;

public final class Exporter implements Runnable {
    private final Theme theme;
    private final Process<Theme> process;

    public Exporter(Theme theme, DataProvider dataProvider, ThreadService threadService, WorldService worldService, Runnable onFinish) {
        this.theme = theme;
        this.process = ExportUtils.createExportChain(threadService, dataProvider, worldService, onFinish);

    }

    @Override
    public void run() {
        process.accept(theme);
    }
}
