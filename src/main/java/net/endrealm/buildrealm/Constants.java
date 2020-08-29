package net.endrealm.buildrealm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    public static final String PREFIX = "§b§lINFO §e>>§7 ";
    public static final String ERROR_PREFIX = "§c§lERR §e>>§7 ";

    public static final String NO_PERMISSION = "You do not have the permission to do this";
    public static final String TRANSACTIONS_RUNNING = "Please wait until your last action succeeds";
    public static final String DRAFT_INVALIDATED = "Draft has become invalid! Please reload the gui";
    public static final String Group_INVALIDATED = "Group has become invalid! Please reload the gui";
    public static final String SYSTEM_WORKING = "The system is currently working. Please try again later!";
}
