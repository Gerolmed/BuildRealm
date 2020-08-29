package net.endrealm.buildrealm.repository;

import net.endrealm.buildrealm.data.entity.Group;

import java.util.List;
import java.util.Optional;

public interface GroupRepository {
    void delete(String id);

    void save(Group id);

    Optional<Group> get(String id);

    List<Group> getAll(List<String> except);
}
