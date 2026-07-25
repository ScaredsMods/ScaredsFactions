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

import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientFactionSavedData {

	public static Map<String, Faction> factions = new ConcurrentHashMap<>();

	public static void addFaction(Faction faction) {
		factions.put(faction.getName(), faction);
	}

	public static Faction getFactionFromPlayer(UUID uuid) {
		for (Faction faction : factions.values()) {
			if (faction.getMembers().containsKey(uuid)) {
				return faction;
			}
		}
		return null;
	}

	public static void putAll(Map<String, Faction> newFactions) {
		factions.clear();
		factions.putAll(newFactions);
	}

	public static void clear() {
		factions.clear();
	}

	public static Faction getFaction(String factionName) {
		return factions.get(factionName);
	}

	public static boolean isEqualFaction(UUID targetUUID) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return false;

		UUID localUUID = mc.player.getUUID();
		if (localUUID.equals(targetUUID)) return true;

		Faction localFaction = getFactionFromPlayer(localUUID);
		Faction targetFaction = getFactionFromPlayer(targetUUID);

		if (localFaction == null || targetFaction == null) return false;
		return localFaction.getName().equals(targetFaction.getName());
	}

}
