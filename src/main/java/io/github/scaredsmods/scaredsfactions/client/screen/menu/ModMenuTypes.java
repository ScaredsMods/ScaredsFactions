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
package io.github.scaredsmods.scaredsfactions.client.screen.menu;

import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.awt.*;

public class ModMenuTypes {

	public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScaredsFactionMod.MOD_ID);

	public static final RegistryObject<MenuType<ManageFactionMenu>> MANAGE_FACTION =
			registerMenuType("manage_faction", ManageFactionMenu::new);

	public static final RegistryObject<MenuType<RenameFactionMenu>> RENAME_FACTION =
			registerMenuType("rename_faction", RenameFactionMenu::new);

	public static final RegistryObject<MenuType<ConfirmTransferOwnershipMenu>> CONFIRM_TRANSFER_OWNERSHIP =
			registerMenuType("confirm_transfer_ownership", ConfirmTransferOwnershipMenu::new);

	public static final RegistryObject<MenuType<TransferOwnershipMenu>> TRANSFER_OWNERSHIP =
			registerMenuType("transfer_ownership", TransferOwnershipMenu::new);

	public static final RegistryObject<MenuType<FactionSettingsMenu>> FACTION_SETTINGS =
			registerMenuType("faction_settings", FactionSettingsMenu::new);

	public static final RegistryObject<MenuType<ViewMembersMenu>> VIEW_MEMBERS =
			registerMenuType("view_members", ViewMembersMenu::new);

	public static final RegistryObject<MenuType<EditStringSettingMenu>> EDIT_STRING_SETTING =
			registerMenuType("edit_string_setting", EditStringSettingMenu::new);

	public static final RegistryObject<MenuType<ConfirmResetBeaconPosMenu>> RESET_BEACON_POS =
			registerMenuType("reset_beacon_pos", ConfirmResetBeaconPosMenu::new);


	private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
		return MENU_TYPES.register(name, () -> IForgeMenuType.create(factory));
	}


	public static void register(IEventBus modEventBus) {
		MENU_TYPES.register(modEventBus);
	}
}
