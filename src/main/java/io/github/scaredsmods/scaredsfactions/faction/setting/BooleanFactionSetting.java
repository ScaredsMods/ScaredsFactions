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
package io.github.scaredsmods.scaredsfactions.faction.setting;

import net.minecraft.nbt.CompoundTag;

public class BooleanFactionSetting extends AbstractFactionSetting<Boolean> {

	public BooleanFactionSetting(Boolean defaultValue, String nbtId, String displayName, String... lore) {
		super(defaultValue, nbtId, displayName, lore);
	}

	public BooleanFactionSetting(Boolean defaultValue, String nbtId, String displayName, boolean isModdedSetting, String modId, String... lore) {
		super(defaultValue, nbtId, displayName, isModdedSetting, modId, lore);
	}

	@Override
	public void save(CompoundTag tag) {
		tag.putBoolean(getNbtId(), get());
	}

	@Override
	public BooleanFactionSetting load(CompoundTag tag) {
		this.set(tag.getBoolean(getNbtId()));
		return this;
	}

	@Override
	public AbstractFactionSetting<Boolean> copy() {
		return new BooleanFactionSetting(getDefaultValue(), getNbtId(), getDisplayName(), getLore());
	}

	public boolean compare(boolean compareBool) {
		return this.get().equals(compareBool);
	}

	public boolean invert() {
		return !this.get();
	}
}
