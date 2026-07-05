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

import net.minecraft.nbt.CompoundTag;

public class ShortFactionSetting extends NumericFactionSetting<Short, ShortFactionSetting> {


	public ShortFactionSetting(Short defaultValue, String nbtId, String displayName, Short step, Short min, Short max, String... lore) {
		super(defaultValue, nbtId, displayName, step, min, max, lore);
	}

	public ShortFactionSetting(Short defaultValue, String nbtId, String displayName, boolean isModdedSetting, String modId, Short step, Short min, Short max, String... lore) {
		super(defaultValue, nbtId, displayName, isModdedSetting, modId, step, min, max, lore);
	}

	@Override
	public void save(CompoundTag tag) {
		tag.putShort(getNbtId(), get());
	}

	@Override
	public ShortFactionSetting load(CompoundTag tag) {
		this.set(tag.getShort(getNbtId()));
		return this;
	}

	@Override
	public ShortFactionSetting copy() {
		return this.isModdedSetting()
				? new ShortFactionSetting(get(), getNbtId(), getDisplayName(), true, getModId(), getStep(), getMin(), getMax(), getLore())
				: new ShortFactionSetting(get(), getNbtId(), getDisplayName(), getStep(), getMin(), getMax(), getLore());
	}

	@Override
	public void increment() {
		set((short) Math.min(get() + 1, Short.MAX_VALUE));
	}

	@Override
	public void decrement() {
		set((short) Math.max(get() - 1, Short.MIN_VALUE));
	}
}
