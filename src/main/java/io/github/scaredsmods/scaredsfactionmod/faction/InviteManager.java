/*
*  Copyright (C) 2025 ScaredRabbitNL
*
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with this program. If not, see <https://www.gnu.org/licenses/>.
*/
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
