package net.endrealm.buildrealm.export;


import lombok.Data;
import lombok.EqualsAndHashCode;
import net.endrealm.buildrealm.data.PieceType;
import net.endrealm.buildrealm.data.entity.Piece;
import net.endrealm.buildrealm.services.ThreadService;
import net.endrealm.buildrealm.utils.Pair;
import net.endrealm.buildrealm.world.WorldService;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;

@EqualsAndHashCode(callSuper = true)
@Data
public class SchematicSaveProcess extends Process<Pair<Piece, File>> {
    private final ThreadService threadService;
    private final WorldService worldService;

    @Override
    public void accept(Pair<Piece, File> pieceFilePair) {
        threadService.runAsync(() -> {
            Piece piece = pieceFilePair.getKey();
            File file = pieceFilePair.getValue();

            if (file.exists()) {
                long lastModified = file.lastModified();
                if (lastModified >= piece.getLastUpdated().getTime()) {
                    runNext(pieceFilePair);
                    return;
                }
            }

            PieceType type = piece.getPieceType();

            worldService.generate(piece.getIdentity(), world -> {
                try {
                    DungeonSchematic dungeonSchematic = new DungeonSchematic(new Vector(0, 70, 0), type.getCorner(), type.getCorner(), world, file);
                    dungeonSchematic.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                worldService.unload(piece.getIdentity(), () -> {
                    runNext(pieceFilePair);
                });
            });
        });
    }
}
