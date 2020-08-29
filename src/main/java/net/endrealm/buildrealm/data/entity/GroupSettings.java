package net.endrealm.buildrealm.data.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.endrealm.realmdrive.annotations.SaveAll;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@SaveAll
public class GroupSettings {
    private String registryName = "invalid";
    private boolean day = false;
    private List<String> monster = new ArrayList<>();
}
