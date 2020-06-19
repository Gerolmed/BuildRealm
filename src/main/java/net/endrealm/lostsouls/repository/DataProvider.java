package net.endrealm.lostsouls.repository;

import net.endrealm.lostsouls.data.PieceType;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.data.entity.Theme;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DataProvider {
    Optional<Draft> getDraft(String key);
    List<Draft> getDraftsByUser(UUID uuid, boolean open);
    void saveDraft(Draft draft);
    String getFreeDraftId();
    void remove(Draft draft);

    Optional<Theme> getTheme(String key);
    void saveTheme(Theme theme);
    List<Theme> getAllThemes();
    void removeTheme(Theme theme);

    List<Draft> getDraftsByThemeAndType(String theme, PieceType type);

    void invalidate(Draft draft);
    void validateCaches();
}
