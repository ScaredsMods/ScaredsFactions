package io.github.scaredsmods.scaredsfactionmod.faction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InviteManager {
    private static final Map<UUID, String> pendingInvites = new HashMap<>();

    public static void addInvite(UUID playerUUID, String factionName) {
        pendingInvites.put(playerUUID, factionName);
    }

    public static boolean hasInvite(UUID playerUUID, String factionName) {
        return factionName.equals(pendingInvites.get(playerUUID));
    }

    public static void removeInvite(UUID playerUUID) {
        pendingInvites.remove(playerUUID);
    }

    public static String getInvite(UUID playerUUID) {
        return pendingInvites.get(playerUUID);
    }
}