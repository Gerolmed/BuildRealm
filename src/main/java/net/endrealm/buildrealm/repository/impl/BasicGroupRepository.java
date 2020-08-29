package net.endrealm.buildrealm.repository.impl;

import lombok.Data;
import net.endrealm.buildrealm.data.entity.Group;
import net.endrealm.buildrealm.data.entity.GroupSettings;
import net.endrealm.buildrealm.data.entity.TypeCategory;
import net.endrealm.buildrealm.repository.GroupRepository;
import net.endrealm.realmdrive.interfaces.DriveService;
import net.endrealm.realmdrive.query.Query;
import net.endrealm.realmdrive.query.compare.ValueNotInOperator;

import java.util.List;
import java.util.Optional;

@Data
public class BasicGroupRepository implements GroupRepository {

    private final DriveService driveService;
    private final String TABLE = "Groups";

    public BasicGroupRepository(DriveService driveService) {
        this.driveService = driveService;
        driveService.getConversionHandler().registerClasses(
                Group.class,
                TypeCategory.class,
                GroupSettings.class
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
    public void save(Group group) {
        driveService.getWriter().write(group, true, queryById(group.getName()));
    }

    @Override
    public Optional<Group> get(String id) {
        Optional<Group> group = Optional.ofNullable(driveService.getReader().readObject(queryById(id), Group.class));
        group.ifPresent(Group::fixList);
        return group;
    }

    @Override
    public List<Group> getAll(List<String> except) {
        ValueNotInOperator<Query> nin = new Query().setTableName(TABLE).addNin()
                .setField("name");
        except.forEach(nin::addValue);
        Query query = nin.close();
        List<Group> groups = driveService.getReader().readAllObjects(query.build(), Group.class);
        groups.forEach(Group::fixList);
        return groups;
    }
}
