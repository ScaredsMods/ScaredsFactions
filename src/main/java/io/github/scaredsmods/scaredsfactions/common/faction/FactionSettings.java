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

import io.github.scaredsmods.scaredsfactions.common.config.ModConfigs;
import io.github.scaredsmods.scaredsfactions.common.config.LanguageOptions;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.AbstractFactionSetting;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.BooleanFactionSetting;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.EnumFactionSetting;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FactionSettings {

	public static List<AbstractFactionSetting<?, ?>> settings = new ArrayList<>();

	public static final BooleanFactionSetting INFO_VISIBLE = register(new BooleanFactionSetting(true, "isInfoVisible", "Show Faction Info", "When set to true, this faction is discoverable with /faction list and /faction info <name>."));
	public static final BooleanFactionSetting VANILLA_FRIENDLY_FIRE = register(new BooleanFactionSetting(false, "enableVanillaFriendlyFire", "Enable Vanilla Friendly Fire", "When set to true, vanilla pvp (axes, swords, etc) within this faction will be enabled.", "This setting is dependent on the mod configuration!"));
	public static final BooleanFactionSetting TACZ_FRIENDLY_FIRE = register(new BooleanFactionSetting(false, "enableTACZFriendlyFire", "Enable Timeless and Classics: Zero Friendly Fire", true, "tacz", "When set to true, pvp with guns from Timeless and Classics: Zero will be enabled.", "This setting does nothing when the mod Timeless and Classics: Zero is not installed and when it is installed, this setting is dependent on this mod's (ScaredsFactions) configuration!"));
	public static final BooleanFactionSetting SBW_FRIENDLY_FIRE = register(new BooleanFactionSetting(false, "enableSBWFriendlyFire", "Enable Superbwarfare Friendly FIre", true, "superbwarfare","When set to true, pvp with guns from Superbwarfare will be enabled.", "This setting does nothing when the mod Superbwarfare is not installed and when it is installed, this setting is dependent on this mod's (ScaredsFactions) configuration!"));
	public static final EnumFactionSetting<Faction.Rank> OWNER_RANK = register(new EnumFactionSetting<>(
			ModConfigs.commonConfig.defaultOwnerRank.get() == LanguageOptions.PREFER_STADHOUDER
					? Faction.Rank.STADHOUDER
					: Faction.Rank.GENERALISSIMUS,
			"ownerRank",
			"Owner Rank",
			Faction.Rank.class,
			new Faction.Rank[] { Faction.Rank.GENERALISSIMUS,  Faction.Rank.STADHOUDER },
			"This setting determines which rank is the highest, Stadhouder or Generalissimus.", "This setting is semi-dependent on the global settings.", "It's default value is whatever the server owner has set in the config. It can be changed, or not!"));

	public static final BooleanFactionSetting ENABLE_FRIENDLY_GLOWING = register(new BooleanFactionSetting(true, "enableFriendlyGlow", "Enable Friendly Player Glowing", "Enables a glowing effect for friendlies, in case you have turned on friendly fire."));
	public static final EnumFactionSetting<ChatFormatting> GLOW_COLOUR = register(new EnumFactionSetting<>(
			ChatFormatting.GREEN,
			"glowColour",
			"Friendly Player Glow Color",
			ChatFormatting.class,
			Arrays.stream(ChatFormatting.values()).filter(ChatFormatting::isColor).toArray(ChatFormatting[]::new),
			"This setting determines which colour appears as an outline if Enable Friendly Player Glowing is enabled."
	));


	public static <T, S extends AbstractFactionSetting<T, S>> S register(S setting) {
		settings.add(setting);
		return setting;
	}

	public static void init() {
	}
}
