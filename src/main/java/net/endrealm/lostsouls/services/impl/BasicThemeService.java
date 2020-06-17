package net.endrealm.lostsouls.services.impl;

import lombok.Data;
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

    private final Set<String> creating = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> deleting = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void createTheme(Theme theme, Consumer<Theme> onCreated, Runnable onFailed) {
        if(dataProvider.getTheme(theme.getName()).isPresent() || deleting.contains(theme.getName()) || creating.contains(theme.getName())) {
            onFailed.run();
            return;
        }
        creating.add(theme.getName());
        threadService.runAsync(() -> {
            dataProvider.saveTheme(theme);
            creating.remove(theme.getName());
            onCreated.accept(theme);
        });
    }

    @Override
    public void deleteTheme(Theme theme, Runnable onDeleted) {
        if(dataProvider.getTheme(theme.getName()).isEmpty() || deleting.contains(theme.getName()) || creating.contains(theme.getName())) {
            return;
        }

        deleting.add(theme.getName());
        threadService.runAsync(() -> {
            dataProvider.removeTheme(theme);
            deleting.remove(theme.getName());

            //TODO set drafts to floating state by removing their target theme
            onDeleted.run();
        });
    }

    @Override
    public void saveTheme(Theme theme, Runnable onSaved) {
        if(deleting.contains(theme.getName()) || creating.contains(theme.getName())) {
            return;
        }

        threadService.runAsync(() -> {
            dataProvider.saveTheme(theme);
            onSaved.run();
        });
    }

    @Override
    public void loadTheme(String id, Consumer<Theme> onFound, Runnable notFound) {
        if(deleting.contains(id) || creating.contains(id)) {
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
}
