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

import io.github.scaredsmods.scaredsfactions.api.client.menu.AbstractConfirmMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;


public class ConfirmResetBeaconPosMenu extends AbstractConfirmMenu {

	public ConfirmResetBeaconPosMenu(int containerId, Inventory inventory) {
		super(ModMenuTypes.RESET_BEACON_POS.get(), containerId);
	}

	public ConfirmResetBeaconPosMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf buf) {
		this(pContainerId, pPlayerInventory);
	}

}
