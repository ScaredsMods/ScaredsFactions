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
package io.github.scaredsmods.scaredsfactionmod.config;

import io.github.scaredsmods.scaredsfactionmod.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactionmod.faction.Faction;
import io.github.scaredsmods.scaredsfactionmod.faction.PersistentData;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.api.FileType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.event.api.ServerUpdateContext;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ModCommonConfig extends Config {

	public ModCommonConfig() {
		super(ScaredsFactionMod.id("common"));
	}

	@Comment("Setting this value to true will enable pvp within factions when using weapons from Timeless and Classics : Zero.")
	public ValidatedBoolean enableTACZFriendlyFire = new ValidatedBoolean(false);

	@Comment("Setting this value to true will enable pvp within factions when using weapons from Superb Warfare")
	public ValidatedBoolean enableSBWFriendlyFire = new ValidatedBoolean(false);

	@Comment("Setting this value to true will enable pvp within factions when using vanilla weapons (axe and sword).")
	public ValidatedBoolean enableVanillaFriendlyFire = new ValidatedBoolean(false);

	@Comment("Whether the player respawns at their faction's beacon.")
	public ValidatedBoolean respawnPlayerAtFactionBeacon = new ValidatedBoolean(true);

	@Override
	public @NotNull FileType fileType() {
		return FileType.TOML;
	}

	@Override
	public void onUpdateServer(@NotNull ServerUpdateContext context) {
		super.onUpdateServer(context);

		var server = context.getServer();
		PersistentData data = PersistentData.get(server.overworld());

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			Faction faction = data.getFactionByPlayer(player.getUUID());
			if (faction == null) continue;
			if (!data.hasBeacon(faction.getName())) continue;

			if (respawnPlayerAtFactionBeacon.get()) {
				player.setRespawnPosition(Level.OVERWORLD, data.getBeaconPos(faction.getName()).above(), 0.0F, true, true);
			} else {
				player.setRespawnPosition(Level.OVERWORLD, null, 0.0F, false, true);
			}
		}
	}
}
