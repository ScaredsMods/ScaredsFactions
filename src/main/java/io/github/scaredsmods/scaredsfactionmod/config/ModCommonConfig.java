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
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.api.FileType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
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

}
