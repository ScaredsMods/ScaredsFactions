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

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ManageFactionMenu extends AbstractContainerMenu {

	public ManageFactionMenu(int pContainerId, Inventory playerInventory) {
		super(ModMenuTypes.MANAGE_FACTION.get(), pContainerId);

		SimpleContainer container = new SimpleContainer(9);

		for (int i = 0; i < 9; ++i) {
			ItemStack pane = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
			pane.setHoverName(Component.literal(""));
			container.setItem(i, pane);
		}


		Player player = playerInventory.player;
		GameProfile profile = player.getGameProfile();
		CompoundTag nbtTransfer = new CompoundTag();
		nbtTransfer.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), profile));

		CompoundTag nbtMembers = new CompoundTag();
		nbtMembers.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), profile));
		addClickableSlot(container, 0, Items.NAME_TAG, "Change Name", ChatFormatting.GREEN);
		addClickableSlot(container, 1, Items.PLAYER_HEAD, "Transfer Ownership", nbtTransfer, ChatFormatting.DARK_PURPLE);
		addClickableSlot(container, 2, Items.PLAYER_HEAD, "View Members", nbtMembers, ChatFormatting.AQUA);
		addClickableSlot(container, 3, Items.PAPER, "Faction Settings", ChatFormatting.GOLD);
		addClickableSlot(container, 8, Items.RED_WOOL, "Close", ChatFormatting.DARK_RED);

		for (int colum = 0; colum < 9; colum++) {
			this.addSlot(new Slot(container, colum, 8 + colum * 18, 18) {
				@Override
				public boolean mayPickup(Player pPlayer) {
					return false;
				}

				@Override
				public boolean mayPlace(ItemStack pStack) {
					return false;
				}
			});
		}

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 49 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 107));
		}
	}

	public ManageFactionMenu(int containerId, Inventory inventory, FriendlyByteBuf friendlyByteBuf) {
		this(containerId, inventory);
	}

	private void addClickableSlot(SimpleContainer container, int index, Item item, String name, ChatFormatting color) {
		ItemStack stack = new ItemStack(item);
		stack.setHoverName(Component.literal(name).withStyle(style -> style.withColor(color).withItalic(false).withBold(true)));
		container.setItem(index, stack);
	}

	private void addClickableSlot(SimpleContainer container, int index, Item item, String name, CompoundTag nbtData, ChatFormatting color) {
		ItemStack stack = new ItemStack(item);
		stack.setTag(nbtData);
		stack.setHoverName(Component.literal(name).withStyle(style -> style.withColor(color).withBold(true).withItalic(false)));
		container.setItem(index, stack);
	}

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return true;
	}
}
