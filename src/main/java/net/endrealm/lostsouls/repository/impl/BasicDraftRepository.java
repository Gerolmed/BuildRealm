package net.endrealm.lostsouls.repository.impl;

import lombok.Data;
import net.endrealm.lostsouls.data.entity.Draft;
import net.endrealm.lostsouls.repository.DraftRepository;
import net.endrealm.realmdrive.interfaces.DriveService;
import net.endrealm.realmdrive.query.Query;
import net.endrealm.realmdrive.query.compare.ValueNotInOperator;
import net.endrealm.realmdrive.query.logics.AndOperator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
public class BasicDraftRepository implements DraftRepository {

    private final DriveService driveService;

    private Query queryById(String id) {
        return new Query().addEq().setField("id").setValue(id).close().build();
    }

    @Override
    public synchronized Optional<Draft> findByKey(String id) {
        return Optional.ofNullable(driveService.getReader().readObject(queryById(id), Draft.class));
    }


    // { $and: [{ "members.uuid": { $eq: "1234" }}, { id: { $nin: ["hijklm", "nopqrs"] } }] }
    @Override
    public synchronized List<Draft> findByMember(UUID uuid, boolean open, List<String> excludedDraftIds) {
        ValueNotInOperator<AndOperator<Query>> nin = new Query()
                .addAnd()
                    .addEq()
                        .setField("members.uuid")
                        .setValue(uuid.toString())
                    .close()
                    .addEq()
                        .setField("open")
                        .setValue(open)
                    .close()
                    .addNin()
                        .setField("id");
        excludedDraftIds.forEach(nin::addValue);

        Query query = nin.close()
                .close()
                .build();

        return driveService.getReader().readAllObjects(query, Draft.class);
    }

    @Override
    public synchronized void save(Draft value) {
        driveService.getWriter().write(value, true, queryById(value.getId()));
    }

    @Override
    public String findFreeKey() {
        String id;
        do {
            id = UUID.randomUUID().toString();
        } while (driveService.getReader().containsObject(queryById(id)));

        return id;
    }

    @Override
    public synchronized void remove(Draft draft) {
        driveService.getWriter().delete(queryById(draft.getId()), 1);
    }
}
