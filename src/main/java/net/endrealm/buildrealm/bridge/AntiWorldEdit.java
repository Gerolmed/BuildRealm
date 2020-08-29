package net.endrealm.buildrealm.bridge;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import net.endrealm.buildrealm.data.entity.Draft;
import org.bukkit.Bukkit;

public class AntiWorldEdit extends AbstractDelegateExtent {
    protected final org.bukkit.World world;
    protected final org.bukkit.entity.Player player;
    protected Draft draft;

    protected AntiWorldEdit(World world, Extent extent, Player player, Draft draft) {
        super(extent);
        this.draft = draft;

        if (world instanceof BukkitWorld) {
            this.world = ((BukkitWorld) world).getWorld();
        } else {
            this.world = Bukkit.getWorld(world.getName());
        }

        this.player = Bukkit.getPlayer(player.getUniqueId());
    }


    @SuppressWarnings("unchecked")
    @Override
    public boolean setBlock(BlockVector3 location, BlockStateHolder block) throws WorldEditException {

        if (draft == null) {
            return false;
        }

        if (!draft.isOpen())
            return false;

        if (draft.hasMember(player.getUniqueId()))
            return super.setBlock(location, block);
        return false;
    }

}
