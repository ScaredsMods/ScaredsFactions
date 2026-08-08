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
package io.github.scaredsmods.scaredsfactions.client.events;

import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.client.screen.*;
import io.github.scaredsmods.scaredsfactions.client.screen.menu.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;


import java.awt.*;

@EventBusSubscriber(modid = ScaredsFactionMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {

	@SubscribeEvent
	public static void onRegisterScreens(RegisterMenuScreensEvent event) {
		event.register(ModMenuTypes.MANAGE_FACTION.get(), ManageFactionScreen::new);
        event.register(ModMenuTypes.FACTION_SETTINGS.get(), FactionSettingsScreen::new);
        event.register(ModMenuTypes.VIEW_MEMBERS.get(), ViewMembersScreen::new);
        event.register(ModMenuTypes.TRANSFER_OWNERSHIP.get(), TransferOwnershipScreen::new);
        event.register(ModMenuTypes.RENAME_FACTION.get(), RenameFactionScreen::new);
        event.register(ModMenuTypes.CONFIRM_TRANSFER_OWNERSHIP.get(), ConfirmTransferOwnershipScreen::new);
        event.register(ModMenuTypes.EDIT_STRING_SETTING.get(), EditStringSettingScreen::new);
        event.register(ModMenuTypes.RESET_BEACON_POS.get(), ConfirmResetBeaconPosScreen::new);

	}
}
