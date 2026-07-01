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
package io.github.scaredsmods.scaredsfactions.faction;

import io.github.scaredsmods.scaredsfactions.faction.setting.AbstractFactionSetting;
import io.github.scaredsmods.scaredsfactions.faction.setting.BooleanFactionSetting;

import java.util.ArrayList;
import java.util.List;

public class FactionSettings {

	public static List<AbstractFactionSetting<?>> settings = new ArrayList<>();

	public static final BooleanFactionSetting INFO_VISIBLE = register(new BooleanFactionSetting(true, "isInfoVisible", "Show Faction Info", "When set to true, this faction is discoverable with /faction list and /faction info <name>."));
	public static final BooleanFactionSetting VANILLA_FRIENDLY_FIRE = register(new BooleanFactionSetting(false, "enableVanillaFriendlyFire", "Enable Vanilla Friendly Fire", "When set to true, vanilla pvp (axes, swords, etc) within this faction will be enabled.", "This setting is dependent on the mod configuration!"));
	public static final BooleanFactionSetting TACZ_FRIENDLY_FIRE = register(new BooleanFactionSetting(false, "enableTACZFriendlyFire", "Enable Timeless and Classics: Zero Friendly Fire", true, "tacz", "When set to true, pvp with guns from Timeless and Classics: Zero will be enabled.", "This setting does nothing when the mod Timeless and Classics: Zero is not installed and when it is installed, this setting is dependent on this mod's (ScaredsFactions) configuration!"));
	public static final BooleanFactionSetting SBW_FRIENDLY_FIRE = register(new BooleanFactionSetting(false, "enableSBWFriendlyFire", "Enable Superbwarfare Friendly FIre", true, "superbwarfare","When set to true, pvp with guns from Superbwarfare will be enabled.", "This setting does nothing when the mod Superbwarfare is not installed and when it is installed, this setting is dependent on this mod's (ScaredsFactions) configuration!"));

	public static BooleanFactionSetting register(BooleanFactionSetting setting) {
		settings.add(setting);
		return setting;
	}

	public static void init() {
	}
}
