package net.endrealm.buildrealm.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.endrealm.buildrealm.data.PermissionLevel;
import net.endrealm.buildrealm.utils.Invalidateble;
import net.endrealm.buildrealm.world.WorldIdentity;
import net.endrealm.realmdrive.annotations.IgnoreVar;
import net.endrealm.realmdrive.annotations.SaveAll;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SaveAll
public class Draft implements Invalidateble {
    private String id;
    private List<Member> members;
    private String note;
    private ForkData forkData;
    private String group;
    private Date lastUpdated;
    private boolean open;

    @IgnoreVar
    private boolean invalid;

    public boolean hasMember(UUID uuid) {
        if (members == null)
            return false;

        for (Member member : members) {
            if (member.getUuid().equals(uuid))
                return true;
        }

        return false;
    }

    public Draft merge(Draft newDraft) {

        Draft draft = new Draft();
        draft.setId(id);
        draft.setForkData(forkData);
        draft.setLastUpdated(newDraft.lastUpdated);
        draft.setNote(note);
        draft.setOpen(open);
        draft.setGroup(group);
        List<Member> members = getMembers() != null ? new ArrayList<>(getMembers()) : new ArrayList<>();
        members.addAll(newDraft.getMembers());
        draft.setMembers(members);

        return draft;
    }

    public WorldIdentity getIdentity() {
        return new WorldIdentity(getId(), open);
    }

    public boolean hasOwner(UUID uuid) {
        if (members == null)
            return false;

        for (Member member : members) {
            if (member.getUuid().equals(uuid) && member.getPermissionLevel() == PermissionLevel.OWNER)
                return true;
        }

        return false;
    }

    public synchronized boolean isInvalid() {
        return invalid;
    }

    public synchronized void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }
}
