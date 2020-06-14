package net.endrealm.lostsouls.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.endrealm.lostsouls.data.PieceType;
import net.endrealm.realmdrive.annotations.SaveAll;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
@SaveAll
public class Piece extends Draft {
    private String number;
    private int forkCount;
    private PieceType pieceType;
    private int categoryId;
}
