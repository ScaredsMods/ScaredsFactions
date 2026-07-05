/*
*  Copyright (C) 2026 ScaredRabbitNL
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
package io.github.scaredsmods.scaredsfactions.common.faction;

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
