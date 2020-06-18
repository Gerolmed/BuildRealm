package net.endrealm.lostsouls.services.impl;

import lombok.Data;
import net.endrealm.lostsouls.data.PieceType;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.data.entity.Theme;
import net.endrealm.lostsouls.repository.DataProvider;
import net.endrealm.lostsouls.services.ThemeService;
import net.endrealm.lostsouls.services.ThreadService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Data
public class BasicThemeService implements ThemeService {

    private final DataProvider dataProvider;
    private final ThreadService threadService;

    private final Set<String> blocked = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void createTheme(Theme theme, Consumer<Theme> onCreated, Runnable onFailed) {
        if(dataProvider.getTheme(theme.getName()).isPresent() || blocked.contains(theme.getName())) {
            onFailed.run();
            return;
        }
        blocked.add(theme.getName());
        threadService.runAsync(() -> {
            dataProvider.saveTheme(theme);
            blocked.remove(theme.getName());
            onCreated.accept(theme);
        });
    }

    @Override
    public void deleteTheme(Theme theme, Runnable onDeleted) {
        if(dataProvider.getTheme(theme.getName()).isEmpty() || blocked.contains(theme.getName())) {
            return;
        }

        blocked.add(theme.getName());
        threadService.runAsync(() -> {
            dataProvider.removeTheme(theme);
            blocked.remove(theme.getName());

            //TODO set drafts to floating state by removing their target theme
            onDeleted.run();
        });
    }

    @Override
    public void saveTheme(Theme theme, Runnable onSaved) {
        if(blocked.contains(theme.getName())) {
            return;
        }

        threadService.runAsync(() -> {
            dataProvider.saveTheme(theme);
            onSaved.run();
        });
    }

    @Override
    public void loadTheme(String id, Consumer<Theme> onFound, Runnable notFound) {
        if(id == null || blocked.contains(id)) {
            notFound.run();
            return;
        }
        threadService.runAsync(() -> {
            dataProvider.getTheme(id).ifPresentOrElse(onFound, notFound);
        });
    }

    @Override
    public void loadAll(Consumer<List<Theme>> onFound) {
        threadService.runAsync(() -> onFound.accept(dataProvider.getAllThemes()));
    }

    @Override
    public synchronized void lock(Theme theme) {
        blocked.add(theme.getName());
    }

    @Override
    public synchronized boolean isLocked(Theme theme) {
        return blocked.contains(theme.getName());
    }

    @Override
    public synchronized void unlock(Theme theme) {
        blocked.remove(theme.getName());
    }
}
