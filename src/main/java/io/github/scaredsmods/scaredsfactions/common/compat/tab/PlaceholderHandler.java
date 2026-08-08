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
package io.github.scaredsmods.scaredsfactions.common.compat.tab;

import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.server.ServerStartingEvent;


public class PlaceholderHandler {

	@SubscribeEvent
	public static void onServerStarting(ServerStartingEvent event) {
		if (!ModList.get().isLoaded("tab")) return;
		TabAPI api = TabAPI.getInstance();
		if (api.getEventBus() == null) return;
		api.getEventBus().register(TabLoadEvent.class, e -> registerPlaceholders());
		registerPlaceholders();
	}

	private static void registerPlaceholders() {
		TabAPI api = TabAPI.getInstance();

		PlaceholderManager placeholderManager = api.getPlaceholderManager();
		placeholderManager.registerPlayerPlaceholder("%player-faction%", 500, player -> {
			ServerPlayer serverplayer = (ServerPlayer) player.getPlayer();
			FactionSavedData data = FactionSavedData.getSavedData(serverplayer.serverLevel());
			Faction faction = data.getFactionFromPlayer(player.getUniqueId());
			return faction == null ? "" : "&7[" + String.format("&r%s" ,faction.getName()) + "&7]" ;
		});
	}


}
