package net.endrealm.lostsouls.data;

import org.bukkit.Material;

public enum PieceType {
    ROOM(Material.STONE_BRICKS, "Rooms", 1),
    START_ROOM(Material.STONE_BRICKS, "Start Rooms", 1),
    FINISH_ROOM(Material.STONE_BRICKS, "Finish Rooms", 1),
    PATH(Material.STONE_BRICK_WALL, "Paths (Straight)", 1),
    PATH_CORNER(Material.STONE_BRICK_WALL, "Paths (Corner)", 1),
    PATH_T(Material.STONE_BRICK_WALL, "Paths (T)", 1),
    PATH_CROSS(Material.STONE_BRICK_WALL, "Paths (Cross)", 1)
    ;

    private final Material material;
    private final String displayName;
    private final int minCount;

    PieceType(Material material, String displayName, int minCount) {
        this.material = material;
        this.displayName = displayName;
        this.minCount = minCount;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinCount() {
        return minCount;
    }
}
