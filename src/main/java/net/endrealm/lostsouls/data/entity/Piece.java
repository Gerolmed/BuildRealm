package net.endrealm.lostsouls.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.endrealm.lostsouls.data.PieceType;
import net.endrealm.realmdrive.annotations.SaveAll;

import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
@SaveAll
public class Piece extends Draft {

    public Piece(String id, List<Member> members, String note, ForkData forkData, String theme, Date lastUpdated, boolean open) {
        super(id, members, note, forkData, theme, lastUpdated, open, false);
    }

    private String number;
    private int forkCount;
    private PieceType pieceType;

    @Override
    public Piece merge(Draft newDraft) {
        Draft merge = super.merge(newDraft);
        Piece piece = new Piece(merge.getId(), merge.getMembers(), merge.getNote(), merge.getForkData(), merge.getTheme(), merge.getLastUpdated(), merge.isOpen());

        piece.setNumber(number);
        piece.setForkCount(forkCount);
        piece.setPieceType(pieceType);

        return piece;
    }
}
