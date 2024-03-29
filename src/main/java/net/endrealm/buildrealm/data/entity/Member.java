package net.endrealm.buildrealm.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.endrealm.buildrealm.data.PermissionLevel;
import net.endrealm.realmdrive.annotations.SaveAll;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SaveAll
public class Member {
    private UUID uuid;
    private PermissionLevel permissionLevel;
}
