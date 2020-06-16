package net.endrealm.lostsouls.repository;

import net.endrealm.lostsouls.data.entity.Theme;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {
    void delete(String id);
    void save(Theme id);
    Optional<Theme> get(String id);
    List<Theme> getAll(List<String> except);
}
