package io.github.scaredsmods.scaredsfactions.faction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InviteManager {

    private static final Map<UUID, String> pendingInvites = new HashMap<>();

    public static void invite(UUID target, String factionName) {
        pendingInvites.put(target, factionName);
    }

    public static void cancelInvite(UUID target) {
        pendingInvites.remove(target);
    }

    public static boolean hasInvite(UUID target) {
        return pendingInvites.containsKey(target);
    }
    public static boolean hasInvite(UUID target, String factionName) {
        return factionName.equals(pendingInvites.get(target));
    }

    public static String getPendingInvite(UUID target) {
        return pendingInvites.get(target);
    }

}
