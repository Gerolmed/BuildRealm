package net.endrealm.lostsouls.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.endrealm.lostsouls.data.PieceType;
import net.endrealm.lostsouls.utils.Invalidateble;
import net.endrealm.realmdrive.annotations.IgnoreVar;
import net.endrealm.realmdrive.annotations.SaveAll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SaveAll
public class Theme implements Invalidateble {

    private static final TypeCategory[] categories = Arrays.stream(PieceType.values()).map(pieceType -> new TypeCategory(pieceType,0,0)).toArray(TypeCategory[]::new);

    private String name;
    private boolean stale;
    //TODO: add settings

    private List<TypeCategory> categoryList;
    @IgnoreVar
    private boolean invalid;

    public synchronized void fixList() {
        List<TypeCategory> newlyAdded = new ArrayList<>();
        for (TypeCategory category: categories) {
            if (categoryList.contains(category)) continue;
            newlyAdded.add(category);
        }
        categoryList.addAll(newlyAdded);
    }
}
