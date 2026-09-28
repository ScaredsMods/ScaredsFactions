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
package io.github.scaredsmods.scaredsfactions.api.common.faction.setting;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public abstract class AbstractFactionSetting<T, S extends AbstractFactionSetting<T, S>> {
	private final String nbtId;
	private final Component displayName;
	private final Component[] lore;
	private final T defaultValue;
	private T value;
	private boolean isModdedSetting;
	private String modId;

	public AbstractFactionSetting(T defaultValue, String nbtId, Component displayName, Component... lore) {
		this.nbtId = nbtId;
		this.displayName = displayName;
		this.lore = lore;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public AbstractFactionSetting(T defaultValue, String nbtId, Component displayName, boolean isModdedSetting, String modId, Component... lore) {
		this(defaultValue, nbtId, displayName, lore);
		this.modId = modId;
		this.isModdedSetting = isModdedSetting;
	}

	public T getDefaultValue() { return this.defaultValue; }

	public String getNbtId() {
		return this.nbtId;
	}

	public Component getDisplayName() {
		return this.displayName;
	}

	public Component[] getLore() {
		return this.lore;
	}

	public T get() {
		return this.value;
	}

	public void set(T value) {
		this.value = value;
	}

	public boolean isModdedSetting() {
		return this.isModdedSetting;
	}

	public String getModId() {
		return this.modId;
	}

	public abstract void save(CompoundTag tag);

	public abstract S load(CompoundTag tag);
	public abstract S copy();
	public abstract void onClick(int mouseButton, Runnable sendUpdate);

	public Component getCurrentValueAsComponent() {
		return Component.literal(get().toString())
				.withStyle(style -> style
						.withColor(ChatFormatting.YELLOW)
						.withItalic(false)
				);
	}

	public boolean compare(T compareValue) {
		return this.get().equals(compareValue);
	}

}
