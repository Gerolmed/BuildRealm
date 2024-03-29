package net.endrealm.buildrealm.world;

public interface WorldAdapter<T> {
    WorldInstance<T> clone(WorldIdentity original, WorldIdentity target);

    WorldInstance<T> load(WorldIdentity identity);

    boolean unload(WorldIdentity identity);

    boolean unload(WorldInstance<T> instance);


    WorldInstance<T> generate(WorldInstance<T> instance);

    void delete(WorldIdentity identity);

    boolean exists(WorldIdentity identity);

    WorldInstance<T> createEmpty(WorldIdentity worldIdentity);

    void save(WorldInstance<T> worldInstance);

    boolean unloadHard(WorldIdentity target);

    boolean unloadHard(WorldInstance<T> instance);

}
