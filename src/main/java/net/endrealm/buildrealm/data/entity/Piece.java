package net.endrealm.buildrealm.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.endrealm.buildrealm.data.PieceType;
import net.endrealm.buildrealm.repository.DataProvider;
import net.endrealm.realmdrive.annotations.SaveAll;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
@SaveAll
public class Piece extends Draft {

    public Piece(String id, List<Member> members, String note, ForkData forkData, String group, Date lastUpdated, boolean open) {
        super(id, members, note, forkData, group, lastUpdated, open, false);
    }

    private String number;
    private int forkCount;
    private PieceType pieceType;

    @Override
    public Piece merge(Draft newDraft) {
        Draft merge = super.merge(newDraft);
        Piece piece = new Piece(merge.getId(), merge.getMembers(), merge.getNote(), merge.getForkData(), merge.getGroup(), merge.getLastUpdated(), merge.isOpen());

        piece.setNumber(number);
        piece.setForkCount(forkCount);
        piece.setPieceType(pieceType);

        return piece;
    }

    public String getEffectiveName(DataProvider dataProvider) {
        if (getForkData() == null) return number;
        Optional<Draft> parent = dataProvider.getDraft(getForkData().getOriginId());
        if (parent.isEmpty()) return number;
        Draft draft = parent.get();
        if (!(draft instanceof Piece)) return number;
        return ((Piece) draft).getEffectiveName(dataProvider) + "_" + number;
    }

    public String getEffectiveDisplayName(DataProvider dataProvider) {
        String effectiveName = getEffectiveName(dataProvider);
        if (effectiveName.length() == 1)
            effectiveName = effectiveName + "_0";
        return effectiveName;
    }
}
