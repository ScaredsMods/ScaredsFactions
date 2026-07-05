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

import java.util.Arrays;

public class EnumFactionSetting<E extends Enum<E>> extends AbstractFactionSetting<E, EnumFactionSetting<E>> {

	private final Class<E> enumClass;
	private final E[] allowedValues;

	public EnumFactionSetting(E defaultValue, String nbtId, String displayName, Class<E> enumClass, E[] allowedValues, String... lore) {
		super(defaultValue, nbtId, displayName, lore);
		this.enumClass = enumClass;
		this.allowedValues = allowedValues;
	}

	public EnumFactionSetting(E defaultValue, String nbtId, String displayName, boolean isModdedSetting, String modId, Class<E> enumClass, E[] allowedValues, String... lore) {
		super(defaultValue, nbtId, displayName, isModdedSetting, modId, lore);
		this.enumClass = enumClass;
		this.allowedValues = allowedValues;
	}

	@Override
	public void save(CompoundTag tag) {
		tag.putString("nbtId", getNbtId());
		tag.putString(getNbtId(), get().name());
	}

	@Override
	public EnumFactionSetting<E> load(CompoundTag tag) {
		this.set(Enum.valueOf(enumClass, tag.getString(getNbtId())));
		return this;
	}

	@Override
	public EnumFactionSetting<E> copy() {
		return this.isModdedSetting() ?
				new EnumFactionSetting<>(getDefaultValue(), getNbtId(), getDisplayName(), true, getModId(), getEnumClass(), getAllowedValues() , getLore())
				: new EnumFactionSetting<>(getDefaultValue(), getNbtId(), getDisplayName(), getEnumClass(), getAllowedValues() , getLore());
	}

	@Override
	public void onClick(int mouseButton, Runnable sendUpdate) {
		E[] values = allowedValues != null ? allowedValues : getEnumClass().getEnumConstants();
		int nextIndex = (Arrays.asList(values).indexOf(get()) + 1) % values.length;
		set(values[nextIndex]);
		sendUpdate.run();
	}

	@Override
	public Component getCurrentValueAsComponent() {
		return Component.literal(get().toString())
				.withStyle(style -> style
						.withColor(ChatFormatting.YELLOW)
						.withItalic(false)
				);
	}

	public Class<E> getEnumClass() {
		return this.enumClass;
	}

	public E[] getAllowedValues() {
		return this.allowedValues;
	}
}
