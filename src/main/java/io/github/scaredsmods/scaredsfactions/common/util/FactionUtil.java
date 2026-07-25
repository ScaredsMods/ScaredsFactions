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
package io.github.scaredsmods.scaredsfactions.common.util;

import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.EnumFactionSetting;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData;
import net.minecraft.server.level.ServerPlayer;

public class FactionUtil {

	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> Class<EnumFactionSetting<E>> enumSetting() {
		return (Class<EnumFactionSetting<E>>) (Class<?>) EnumFactionSetting.class;
	}


	public static boolean isEqualFaction(ServerPlayer player, ServerPlayer target) {
		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction playerFaction = data.getFactionFromPlayer(player.getUUID());
		Faction targetFaction = data.getFactionFromPlayer(target.getUUID());
		return playerFaction.equals(targetFaction);
	}
}
