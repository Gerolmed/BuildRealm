package net.endrealm.lostsouls.services;

import net.endrealm.lostsouls.data.entity.Theme;
import org.bukkit.util.Consumer;

public interface ThemeService {
    void createTheme(Theme theme, Consumer<Theme> onCreated, Runnable onFailed);
    void deleteTheme(Theme theme, Runnable onDeleted);
    void saveTheme(Theme theme, Runnable onSaved);
    void loadTheme(String id, Consumer<Theme> onFound, Runnable notFound);
}
