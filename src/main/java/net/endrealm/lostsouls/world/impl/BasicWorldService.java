package net.endrealm.lostsouls.world.impl;

import lombok.RequiredArgsConstructor;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.world.WorldAdapter;
import net.endrealm.lostsouls.world.WorldIdentity;
import net.endrealm.lostsouls.world.WorldInstance;
import net.endrealm.lostsouls.world.WorldService;
import org.bukkit.World;

import java.util.HashMap;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class BasicWorldService<T> implements WorldService {

    private final HashMap<WorldIdentity, WorldInstance<?>> activeInstances = new HashMap<>();
    private final WorldAdapter<T> worldAdapter;
    private final ThreadService threadService;

    @Override
    public synchronized void generate(WorldIdentity worldIdentity, Consumer<World> worldConsumer) {
        if(activeInstances.containsKey(worldIdentity)) {
            worldConsumer.accept(activeInstances.get(worldIdentity).getBukkitWorld().get());
        }
        threadService.runAsync(() -> {
            final WorldInstance<T> instance;
            if(worldAdapter.exists(worldIdentity)) {
                instance = worldAdapter.load(worldIdentity);
            } else {
                instance = worldAdapter.createEmpty(worldIdentity);
            }
            threadService.runSync(
                    () -> {
                        WorldInstance<T> filledInstance = worldAdapter.generate(instance);
                        activeInstances.put(worldIdentity, filledInstance);
                        worldConsumer.accept(filledInstance.getBukkitWorld().get());
                    }
            );
        });
    }

    @Override
    public synchronized void clone(WorldIdentity worldIdentity, WorldIdentity target, Runnable onSuccess) {
        delete(target, () -> {
            worldAdapter.clone(worldIdentity, target);
            onSuccess.run();
        });
    }

    @Override
    public synchronized void unload(WorldIdentity worldIdentity, Runnable onSuccess) {
        if(!activeInstances.containsKey(worldIdentity)) {
            onSuccess.run();
            return;
        }
        threadService.runSync(() -> {
            onSuccess.run();
            worldAdapter.unload(worldIdentity);
            onSuccess.run();
        });
    }

    @Override
    public synchronized void delete(WorldIdentity target, Runnable onSuccess) {
        threadService.runSync(() -> {
            if(activeInstances.containsKey(target)) {
                activeInstances.remove(target);
                worldAdapter.unload(target);
            }
            threadService.runAsync(() -> {
                worldAdapter.delete(target);
                onSuccess.run();
            });
        });
    }

    @Override
    public boolean isLoaded(WorldIdentity worldIdentity) {
        return activeInstances.containsKey(worldIdentity);
    }

    @Override
    public void replace(WorldIdentity old, WorldIdentity newIdentity, Runnable onSuccess) {
        unload(newIdentity, () -> {
            delete(old, () -> {
                clone(newIdentity, old, () -> {
                    delete(newIdentity, onSuccess);
                });

            });
        });
    }
}
