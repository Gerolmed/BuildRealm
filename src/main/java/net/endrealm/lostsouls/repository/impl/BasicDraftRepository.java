package net.endrealm.lostsouls.repository.impl;

import lombok.Data;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.repository.DraftRepository;
import net.endrealm.realmdrive.interfaces.DriveService;
import net.endrealm.realmdrive.query.Query;

import java.util.Optional;

@Data
public class BasicDraftRepository implements DraftRepository {

    private final DriveService driveService;

    private Query queryById(String id) {
        return new Query().addEq().setField("id").setValue(id).close().build();
    }

    @Override
    public Optional<Draft> findByKey(String id) {
        return Optional.ofNullable(driveService.getReader().readObject(queryById(id), Draft.class));
    }

    @Override
    public void save(Draft value) {
        driveService.getWriter().write(value, true, queryById(value.getId()));
    }
}
