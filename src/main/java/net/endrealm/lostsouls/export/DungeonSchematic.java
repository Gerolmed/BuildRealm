package net.endrealm.lostsouls.export;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class DungeonSchematic {
    private final Vector bottom;
    private final Vector top;
    private final World world;
    private final File file;

    public DungeonSchematic(Vector center, Vector bottom, Vector top, World world, File file) {
        this.bottom = center.clone().subtract(bottom);
        this.top = center.clone().add(top);
        this.world = world;
        this.file = file;
    }

    public void save() throws IOException {
        //file.createNewFile();
        CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(world),
                BlockVector3.at(bottom.getBlockX(), bottom.getBlockY(), bottom.getBlockZ()),
                BlockVector3.at(top.getBlockX(), top.getBlockY(), top.getBlockZ()));
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(world), -1)) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, region, clipboard, region.getMinimumPoint()
            );
            // configure here
            Operations.complete(forwardExtentCopy);

            try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
                writer.write(clipboard);
            }

        } catch (WorldEditException e) {
            e.printStackTrace();
        }

    }
}
