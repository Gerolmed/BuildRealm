package net.endrealm.buildrealm.services;

import net.endrealm.buildrealm.data.entity.Group;

import java.util.List;
import java.util.function.Consumer;

public interface GroupService {
    void createGroup(Group group, Consumer<Group> onCreated, Runnable onFailed);

    void deleteGroup(Group group, Runnable onDeleted);

    void saveGroup(Group group, Runnable onSaved);

    void loadGroup(String id, Consumer<Group> onFound, Runnable notFound);

    void loadAll(Consumer<List<Group>> onFound);

    void lock(Group group);

    boolean isLocked(Group group);

    void unlock(Group group);
}
