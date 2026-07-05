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

public class LongFactionSetting extends NumericFactionSetting<Long, LongFactionSetting> {

	public LongFactionSetting(Long defaultValue, String nbtId, String displayName, Long step, Long min, Long max, String... lore) {
		super(defaultValue, nbtId, displayName, step, min, max, lore);
	}

	public LongFactionSetting(Long defaultValue, String nbtId, String displayName, boolean isModdedSetting, String modId, Long step, Long min, Long max, String... lore) {
		super(defaultValue, nbtId, displayName, isModdedSetting, modId, step, min, max, lore);
	}

	@Override
	public void save(CompoundTag tag) {
		tag.putLong(this.getNbtId(), this.get());
	}

	@Override
	public LongFactionSetting load(CompoundTag tag) {
		this.set(tag.getLong(this.getNbtId()));
		return this;
	}

	@Override
	public LongFactionSetting copy() {
		return this.isModdedSetting()
				? new LongFactionSetting(getDefaultValue(), getNbtId(), getDisplayName(), true, getModId(), getStep(), getMin(), getMax(), getLore())
				: new LongFactionSetting(getDefaultValue(), getNbtId(), getDisplayName(), getStep(), getMin(), getMax(), getLore());
	}

	@Override
	public void increment() {
		set(Math.min(get() + 1, Long.MAX_VALUE));
	}

	@Override
	public void decrement() {
		set(Math.max(get() - 1, Long.MIN_VALUE));
	}

}
