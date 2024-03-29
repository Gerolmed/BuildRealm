package net.endrealm.buildrealm.world;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
@Getter
@Setter
public class WorldIdentity {
    private final String worldName;
    private final boolean open;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorldIdentity)) return false;
        WorldIdentity that = (WorldIdentity) o;
        return worldName.equals(that.worldName);
    }

    @Override
    public int hashCode() {
        return worldName.hashCode();
    }
}
