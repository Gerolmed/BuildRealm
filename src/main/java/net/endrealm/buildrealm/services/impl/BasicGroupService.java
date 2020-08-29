package net.endrealm.buildrealm.services.impl;

import lombok.Data;
import net.endrealm.buildrealm.data.entity.Group;
import net.endrealm.buildrealm.repository.DataProvider;
import net.endrealm.buildrealm.services.GroupService;
import net.endrealm.buildrealm.services.ThreadService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Data
public class BasicGroupService implements GroupService {

    private final DataProvider dataProvider;
    private final ThreadService threadService;

    private final Set<String> blocked = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void createGroup(Group group, Consumer<Group> onCreated, Runnable onFailed) {
        if (dataProvider.getGroup(group.getName()).isPresent() || blocked.contains(group.getName())) {
            onFailed.run();
            return;
        }
        blocked.add(group.getName());
        threadService.runAsync(() -> {
            dataProvider.saveGroup(group);
            blocked.remove(group.getName());
            onCreated.accept(group);
        });
    }

    @Override
    public void deleteGroup(Group group, Runnable onDeleted) {
        if (dataProvider.getGroup(group.getName()).isEmpty() || blocked.contains(group.getName())) {
            return;
        }

        blocked.add(group.getName());
        threadService.runAsync(() -> {
            dataProvider.removeGroup(group);
            blocked.remove(group.getName());

            //TODO set drafts to floating state by removing their target Group
            onDeleted.run();
        });
    }

    @Override
    public void saveGroup(Group group, Runnable onSaved) {
        if (blocked.contains(group.getName())) {
            return;
        }

        threadService.runAsync(() -> {
            dataProvider.saveGroup(group);
            onSaved.run();
        });
    }

    @Override
    public void loadGroup(String id, Consumer<Group> onFound, Runnable notFound) {
        if (id == null || blocked.contains(id)) {
            notFound.run();
            return;
        }
        threadService.runAsync(() -> {
            dataProvider.getGroup(id).ifPresentOrElse(onFound, notFound);
        });
    }

    @Override
    public void loadAll(Consumer<List<Group>> onFound) {
        threadService.runAsync(() -> onFound.accept(dataProvider.getAllGroups()));
    }

    @Override
    public synchronized void lock(Group group) {
        blocked.add(group.getName());
    }

    @Override
    public synchronized boolean isLocked(Group group) {
        return blocked.contains(group.getName());
    }

    @Override
    public synchronized void unlock(Group group) {
        blocked.remove(group.getName());
    }
}
