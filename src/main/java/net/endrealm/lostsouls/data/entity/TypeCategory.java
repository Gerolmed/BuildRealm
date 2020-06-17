package net.endrealm.lostsouls.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.endrealm.lostsouls.data.PieceType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeCategory {
    private PieceType type;

    @EqualsAndHashCode.Exclude
    private int mainPointer;
    @EqualsAndHashCode.Exclude
    private int pieceCount;

    public TypeCategory duplicate() {
        return new TypeCategory(type, mainPointer, pieceCount);
    }
}
