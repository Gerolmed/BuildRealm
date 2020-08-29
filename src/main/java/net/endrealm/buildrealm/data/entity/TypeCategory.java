package net.endrealm.buildrealm.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.endrealm.buildrealm.data.PieceType;
import net.endrealm.realmdrive.annotations.SaveAll;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SaveAll
public class TypeCategory {
    private PieceType type;

    @EqualsAndHashCode.Exclude
    private int mainPointer;
    @EqualsAndHashCode.Exclude
    private int pieceCount;

    public TypeCategory duplicate() {
        return new TypeCategory(type, mainPointer, pieceCount);
    }

    public boolean isComplete() {
        return pieceCount >= type.getMinCount();
    }
}
