package net.endrealm.buildrealm.data.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.endrealm.realmdrive.annotations.SaveAll;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SaveAll
public class ForkData {
    private String originId;
}
