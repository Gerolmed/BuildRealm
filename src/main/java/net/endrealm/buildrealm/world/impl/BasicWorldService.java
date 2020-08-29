package net.endrealm.buildrealm.world.impl;

import lombok.RequiredArgsConstructor;
import net.endrealm.buildrealm.services.ThreadService;
import net.endrealm.buildrealm.world.WorldAdapter;
import net.endrealm.buildrealm.world.WorldIdentity;
import net.endrealm.buildrealm.world.WorldInstance;
import net.endrealm.buildrealm.world.WorldService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class BasicWorldService<T> implements WorldService {

    private final HashMap<WorldIdentity, WorldInstance<T>> activeInstances = new HashMap<>();
    private final WorldAdapter<T> worldAdapter;
    private final ThreadService threadService;

    @Override
    public synchronized void generate(WorldIdentity worldIdentity, Consumer<World> worldConsumer) {
        if (activeInstances.containsKey(worldIdentity)) {
            worldConsumer.accept(activeInstances.get(worldIdentity).getBukkitWorld().get());
            return;
        }
        threadService.runAsync(() -> {
            final WorldInstance<T> instance;
            if (worldAdapter.exists(worldIdentity)) {
                instance = worldAdapter.load(worldIdentity);
            } else {
                instance = worldAdapter.createEmpty(worldIdentity);
            }
            if (instance == null) {
                worldConsumer.accept(null);
            }
            threadService.runSync(
                    () -> {
                        WorldInstance<T> filledInstance = worldAdapter.generate(instance);
                        activeInstances.put(worldIdentity, filledInstance);
                        filledInstance.getBukkitWorld().ifPresent(world -> {
                            Block block = new Location(world, 0, 70, 0).getBlock();
                            if (block.isEmpty()) {
                                block.setType(Material.WET_SPONGE);
                            }
                        });
                        worldConsumer.accept(filledInstance.getBukkitWorld().get());
                    }
            );
        });
    }

    @Override
    public synchronized void clone(WorldIdentity worldIdentity, WorldIdentity target, Runnable onSuccess) {
        delete(target, () -> {
            threadService.runSync(() -> {
                unload(worldIdentity, () -> {
                    threadService.runAsync(() -> {
                        worldAdapter.clone(worldIdentity, target);
                        onSuccess.run();
                    });
                });
            });
        });
    }

    @Override
    public synchronized void unload(WorldIdentity worldIdentity, Runnable onSuccess) {
        if (!activeInstances.containsKey(worldIdentity)) {
            onSuccess.run();
            return;
        }
        threadService.runSync(() -> {
            WorldInstance<T> instance = activeInstances.remove(worldIdentity);
            worldAdapter.unload(instance.getIdentity());
            onSuccess.run();
        });
    }

    @Override
    public synchronized void delete(WorldIdentity target, Runnable onSuccess) {
        threadService.runSync(() -> {
            if (activeInstances.containsKey(target)) {
                activeInstances.remove(target);
                worldAdapter.unloadHard(target);
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

    @Override
    public void save(WorldIdentity identity) {
        if (!activeInstances.containsKey(identity))
            return;
        worldAdapter.save(activeInstances.get(identity));
    }

    @Override
    public synchronized void unloadSync(WorldIdentity worldIdentity) {
        if (!activeInstances.containsKey(worldIdentity)) {
            return;
        }
        WorldInstance<T> instance = activeInstances.remove(worldIdentity);
        worldAdapter.unload(instance.getIdentity());
    }
}
