package net.endrealm.lostsouls.export;


import lombok.Data;
import lombok.EqualsAndHashCode;
import net.endrealm.lostsouls.data.PieceType;
import net.endrealm.lostsouls.data.entity.Piece;
import net.endrealm.lostsouls.services.ThreadService;
import net.endrealm.lostsouls.utils.Pair;
import net.endrealm.lostsouls.world.WorldService;
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

            if(file.exists()) {
                long lastModified = file.lastModified();
                if(lastModified >= piece.getLastUpdated().getTime()) {
                    runNext(pieceFilePair);
                    return;
                }
            }

            PieceType type = piece.getPieceType();

            worldService.generate(piece.getIdentity(), world -> {
                try {
                    DungeonSchematic dungeonSchematic = new DungeonSchematic(new Vector(0,70,0), type.getCorner(), type.getCorner(), world, file);
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
