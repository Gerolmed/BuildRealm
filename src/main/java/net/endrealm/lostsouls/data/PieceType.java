package net.endrealm.lostsouls.data;

import org.bukkit.Material;
import org.bukkit.util.Vector;

public enum PieceType {
    ROOM(Material.STONE_BRICKS, "Rooms", 1, 29, 21),
    START_ROOM(Material.STONE_BRICKS, "Start Rooms", 1, 29, 21),
    FINISH_ROOM(Material.STONE_BRICKS, "Finish Rooms", 1, 29, 21),
    PATH(Material.STONE_BRICK_WALL, "Paths (Straight)", 1, 29, 21),
    PATH_CORNER(Material.STONE_BRICK_WALL, "Paths (Corner)", 1, 29, 21),
    PATH_T(Material.STONE_BRICK_WALL, "Paths (T)", 1, 29, 21),
    PATH_CROSS(Material.STONE_BRICK_WALL, "Paths (Cross)", 1, 29, 21)
    ;

    private final Material material;
    private final String displayName;
    private final int minCount;
    private final int width;
    private final int height;

    PieceType(Material material, String displayName, int minCount, int width, int height) {
        this.material = material;
        this.displayName = displayName;
        this.minCount = minCount;
        this.width = width;
        this.height = height;
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

    private int halfWidth() {
        return (width-1)/2;
    }
    private int halfHeight() {
        return (width-1)/2;
    }

    public Vector getCorner() {
        return new Vector(halfWidth(), halfHeight(), halfWidth());
    }
}
