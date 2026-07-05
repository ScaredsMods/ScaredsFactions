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
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransferOwnershipMenu extends AbstractContainerMenu {

	private final Map<GameProfile, Faction.Rank> members;
	private final List<GameProfile> slots = new ArrayList<>();

	public TransferOwnershipMenu(int pContainerId, Inventory pInventory, FriendlyByteBuf pBuffer) {
		this(pContainerId, pInventory, read(pBuffer));
	}

	public TransferOwnershipMenu(int pContainerId, Inventory playerInv, Map<GameProfile, Faction.Rank> members) {
		super(ModMenuTypes.TRANSFER_OWNERSHIP.get(), pContainerId);
		this.members = members;
		SimpleContainer container = new SimpleContainer(54);

		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack pane = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
			pane.setHoverName(Component.literal(""));
			container.setItem(i, pane);
		}


		ItemStack back = new ItemStack(Items.RED_WOOL);
		back.setHoverName(Component.literal("Back").withStyle(style -> style.withColor(ChatFormatting.DARK_RED).withBold(true).withItalic(false)));
		container.setItem(49, back);

		int index = 0;
		for (Map.Entry<GameProfile, Faction.Rank> entry : members.entrySet()) {
			if (index == 49) continue;

			GameProfile profile = entry.getKey();
			Faction.Rank rank = entry.getValue();

			ItemStack head = new ItemStack(Items.PLAYER_HEAD);
			CompoundTag nbt = new CompoundTag();
			nbt.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), profile));
			head.setTag(nbt);
			head.setHoverName(Component.literal(profile.getName()).withStyle(style -> style.withColor(ChatFormatting.YELLOW).withItalic(false)));

			CompoundTag display = head.getOrCreateTagElement("display");
			ListTag lore = new ListTag();
			lore.add(StringTag.valueOf(Component.Serializer.toJson(
					Component.literal("Rank: " + rank.getName())
							.withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false))
			)));

			lore.add(StringTag.valueOf(Component.Serializer.toJson(
					Component.literal("Choose this player as your successor?")
							.withStyle(style -> style.withColor(ChatFormatting.GREEN).withItalic(false))
			)));

			display.put("Lore", lore);
			container.setItem(index, head);
			slots.add(entry.getKey());
			index++;
		}

		for (int j = 0; j < 6; j++) {
			for (int k = 0; k < 9; k++) {
				this.addSlot(new Slot(container, k + j * 9, 8 + k * 18, 18 + j * 18) {
					@Override
					public boolean mayPickup(Player pPlayer) { return false; }
					@Override
					public boolean mayPlace(ItemStack pStack) { return false; }
				});
			}
		}

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
			}
		}

		for (int col = 0; col < 9; col++) {
			this.addSlot(new Slot(playerInv, col, 8 + col * 18, 198));
		}
	}


	public static Map<GameProfile, Faction.Rank> read(FriendlyByteBuf buf) {
		int size = buf.readInt();
		Map<GameProfile, Faction.Rank> members = new HashMap<>();
		for (int i = 0; i < size; i++) {
			GameProfile profile = buf.readGameProfile();
			Faction.Rank rank = buf.readEnum(Faction.Rank.class);
			members.put(profile, rank);
		}
		return members;
	}

	public Map<GameProfile, Faction.Rank> getMembers() {
		return this.members;
	}

	public List<GameProfile> getSlots() {
		return this.slots;
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
