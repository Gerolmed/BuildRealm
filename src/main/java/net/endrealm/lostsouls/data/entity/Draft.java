package net.endrealm.lostsouls.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.endrealm.realmdrive.annotations.SaveAll;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SaveAll
public class Draft {
    private String id;
    private List<Member> members;
    private String note;
    private ForkData forkData;
    private String theme;
    private Date lastUpdated;
}
