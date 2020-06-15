package net.endrealm.lostsouls.world;

import lombok.Data;

@Data
public class WorldIdentity {
    private final String worldName;
    private final boolean open;

    @Override
    public int hashCode() {
        return worldName.hashCode();
    }
}
