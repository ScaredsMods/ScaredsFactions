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
package io.github.scaredsmods.scaredsfactions.common;


import com.mojang.logging.LogUtils;
import io.github.scaredsmods.scaredsfactions.client.screen.menu.ModMenuTypes;
import io.github.scaredsmods.scaredsfactions.common.command.argument.ModCommandArgumentTypes;
import io.github.scaredsmods.scaredsfactions.common.compat.tab.PlaceholderHandler;
import io.github.scaredsmods.scaredsfactions.common.component.ModDataComponents;
import io.github.scaredsmods.scaredsfactions.common.config.ModConfigs;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSettings;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;


@Mod(ScaredsFactionMod.MOD_ID)
public class ScaredsFactionMod {
	public static final String MOD_ID = "scaredsfactions";
	public static final Logger LOGGER = LogUtils.getLogger();

	public ScaredsFactionMod(IEventBus bus, ModContainer container) {
		FactionSettings.init();
		ModConfigs.init();
		ModMenuTypes.register(bus);
		ModCommandArgumentTypes.register(bus);
        ModDataComponents.register(bus);
        if (ModList.get().isLoaded("tab")) {
            NeoForge.EVENT_BUS.register(PlaceholderHandler.class);
        }
	}

	public static ResourceLocation id(String name) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
	}

	public static String permission(String perm) {
		return MOD_ID + "." + perm;
	}



}
