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

public class IntFactionSetting extends NumericFactionSetting<Integer, IntFactionSetting> {

	public IntFactionSetting(Integer defaultValue, String nbtId, String displayName, Integer step, Integer min, Integer max, String... lore) {
		super(defaultValue, nbtId, displayName, step, min, max, lore);
	}

	public IntFactionSetting(Integer defaultValue, String nbtId, String displayName, boolean isModdedSetting, String modId, Integer step, Integer min, Integer max, String... lore) {
		super(defaultValue, nbtId, displayName, isModdedSetting, modId, step, min, max, lore);
	}

	@Override
	public void save(CompoundTag tag) {
		tag.putInt(getNbtId(), get());
	}

	@Override
	public IntFactionSetting load(CompoundTag tag) {
		tag.getInt(getNbtId());
		return this;
	}

	@Override
	public IntFactionSetting copy() {
		return this.isModdedSetting()
				? new IntFactionSetting(getDefaultValue(), getNbtId(), getDisplayName(), true, getModId(), getStep(), getMin(), getMax(), getLore())
				: new IntFactionSetting(getDefaultValue(), getNbtId(), getDisplayName(), getStep(), getMin(), getMax(), getLore());
	}

	@Override
	public void increment() {
		set(Math.min(get() + 1, Integer.MAX_VALUE));
	}

	@Override
	public void decrement() {
		set(Math.max(get() - 1, Integer.MIN_VALUE));
	}

}
