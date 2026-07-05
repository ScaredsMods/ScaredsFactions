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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class EditStringSettingMenu extends AbstractContainerMenu {

	private final String nbtId;

	public EditStringSettingMenu(int containerId, Inventory inventory, String nbtId) {
		super(ModMenuTypes.EDIT_STRING_SETTING.get(), containerId);
		this.nbtId = nbtId;
	}

	public EditStringSettingMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf buf) {
		this(pContainerId, pPlayerInventory, buf.readUtf());
	}

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return true;
	}

	public String getNbtId() {
		return this.nbtId;
	}
}
