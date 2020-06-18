package net.endrealm.lostsouls.services;

import net.endrealm.lostsouls.data.PieceType;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.data.entity.Theme;

import java.util.List;
import java.util.function.Consumer;

public interface ThemeService {
    void createTheme(Theme theme, Consumer<Theme> onCreated, Runnable onFailed);
    void deleteTheme(Theme theme, Runnable onDeleted);
    void saveTheme(Theme theme, Runnable onSaved);
    void loadTheme(String id, Consumer<Theme> onFound, Runnable notFound);

    void loadAll(Consumer<List<Theme>> onFound);

    void lock(Theme theme);
    boolean isLocked(Theme theme);
    void unlock(Theme theme);
}
