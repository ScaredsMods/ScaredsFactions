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

import io.github.scaredsmods.scaredsfactions.faction.FactionSettings;
import io.github.scaredsmods.scaredsfactions.faction.setting.AbstractFactionSetting;
import io.github.scaredsmods.scaredsfactions.faction.setting.BooleanFactionSetting;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import java.util.List;

public class FactionSettingsMenu extends AbstractContainerMenu {

	private final List<AbstractFactionSetting<?>> settings;

	public FactionSettingsMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
		this(containerId, playerInventory, readSettingsFromBuf(buf));
	}

	public FactionSettingsMenu(int containerId, Inventory playerInventory, List<AbstractFactionSetting<?>> settings) {
		super(ModMenuTypes.FACTION_SETTINGS.get(), containerId);
		this.settings = settings;
		SimpleContainer container = new SimpleContainer(54);


		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack pane = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
			pane.setHoverName(Component.literal(""));
			container.setItem(i, pane);
		}

		ItemStack back = new ItemStack(Items.RED_WOOL);
		back.setHoverName(Component.literal("Back").withStyle(style -> style.withColor(ChatFormatting.DARK_RED).withBold(true).withItalic(false)));
		container.setItem(49, back);

		for (int i = 0; i < this.settings.size(); i++) {
			if (i == 49) continue;
			AbstractFactionSetting<?> setting = settings.get(i);
			ItemStack settingStack = new ItemStack(Items.PAPER);
			CompoundTag display = settingStack.getOrCreateTagElement("display");
			settingStack.setHoverName(Component.literal(setting.getDisplayName()).withStyle(style -> style.withColor(ChatFormatting.YELLOW).withItalic(false)));

			ListTag lore = new ListTag();
			for (String loreLine : setting.getLore()) {
				lore.add(StringTag.valueOf(Component.Serializer.toJson(
						Component.literal(loreLine)
								.withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false))
				)));
			}

			if (setting instanceof BooleanFactionSetting boolSetting) {
				boolean currentValue = boolSetting.get();
				lore.add(StringTag.valueOf(Component.Serializer.toJson(
						Component.literal("Current value: ")
								.withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false))
								.append(Component.literal(String.valueOf(currentValue))
										.withStyle(style -> style
												.withColor(currentValue ? ChatFormatting.GREEN : ChatFormatting.RED)
												.withItalic(false)))
				)));
			}

			lore.add(StringTag.valueOf(Component.Serializer.toJson(
					Component.literal("Click to change setting!")
							.withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false))
			)));
			display.put("Lore", lore);
			container.setItem(i, settingStack);
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
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
			}
		}

		for (int col = 0; col < 9; col++) {
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
		}
	}

	public List<AbstractFactionSetting<?>> getSettings() {
		return this.settings;
	}

	private static List<AbstractFactionSetting<?>> readSettingsFromBuf(FriendlyByteBuf buf) {
		List<AbstractFactionSetting<?>> result = new ArrayList<>();
		for (AbstractFactionSetting<?> template : FactionSettings.settings) {
			if (template instanceof BooleanFactionSetting) {
				BooleanFactionSetting copy = (BooleanFactionSetting) template.copy();
				copy.set(buf.readBoolean());
				result.add(copy);
			}
		}
		return result;
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
