package net.endrealm.lostsouls.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.endrealm.lostsouls.utils.Invalidateble;
import net.endrealm.realmdrive.annotations.IgnoreVar;
import net.endrealm.realmdrive.annotations.SaveAll;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SaveAll
public class Theme implements Invalidateble {

    private String name;
    private boolean stale;
    //TODO: add settings

    @IgnoreVar
    private boolean invalid;
}
