package net.endrealm.buildrealm.data;

public enum PermissionLevel {
    COLLABORATOR, OWNER;

    public PermissionLevel cycle() {
        int ordinal = ordinal() + 1;
        if (ordinal > values().length - 1) {
            ordinal = 0;
        }
        return values()[ordinal];
    }
}
