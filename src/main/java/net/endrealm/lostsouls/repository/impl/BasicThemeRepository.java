package net.endrealm.lostsouls.repository.impl;

import lombok.Data;
import net.endrealm.lostsouls.data.entity.*;
import net.endrealm.lostsouls.repository.ThemeRepository;
import net.endrealm.realmdrive.interfaces.DriveService;
import net.endrealm.realmdrive.query.Query;
import net.endrealm.realmdrive.query.compare.ValueNotInOperator;

import java.util.List;
import java.util.Optional;

@Data
public class BasicThemeRepository implements ThemeRepository {

    private final DriveService driveService;
    private final String TABLE = "themes";

    public BasicThemeRepository(DriveService driveService) {
        this.driveService = driveService;
        driveService.getConversionHandler().registerClasses(
                Theme.class,
                TypeCategory.class,
                ThemeSettings.class
        );
    }

    private Query queryById(String id) {
        return new Query().setTableName(TABLE).addEq().setField("name").setValue(id).close().build();
    }


    @Override
    public void delete(String id) {
        driveService.getWriter().delete(queryById(id), 1);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void save(Theme theme) {
        driveService.getWriter().write(theme, true, queryById(theme.getName()));
    }

    @Override
    public Optional<Theme> get(String id) {
        Optional<Theme> theme = Optional.ofNullable(driveService.getReader().readObject(queryById(id), Theme.class));
        theme.ifPresent(Theme::fixList);
        return theme;
    }

    @Override
    public List<Theme> getAll(List<String> except) {
        ValueNotInOperator<Query> nin = new Query().setTableName(TABLE).addNin()
                .setField("name");
        except.forEach(nin::addValue);
        Query query = nin.close();
        List<Theme> themes = driveService.getReader().readAllObjects(query.build(), Theme.class);
        themes.forEach(Theme::fixList);
        return themes;
    }
}
