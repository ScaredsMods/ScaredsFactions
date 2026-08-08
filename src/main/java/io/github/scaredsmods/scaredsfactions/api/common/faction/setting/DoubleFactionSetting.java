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
import net.minecraft.network.RegistryFriendlyByteBuf;

public class DoubleFactionSetting extends NumericFactionSetting<Double, DoubleFactionSetting>{


	public DoubleFactionSetting(Double defaultValue, String nbtId, String displayName, Double step, Double min, Double max, String... lore) {
		super(defaultValue, nbtId, displayName, step, min, max, lore);
	}

	public DoubleFactionSetting(Double defaultValue, String nbtId, String displayName, boolean isModdedSetting, String modId, Double step, Double min, Double max, String... lore) {
		super(defaultValue, nbtId, displayName, isModdedSetting, modId, step, min, max, lore);
	}

	@Override
	public void save(CompoundTag tag) {
		tag.putDouble(this.getNbtId(), this.get());
	}

	@Override
	public DoubleFactionSetting load(CompoundTag tag) {
		this.set(tag.getDouble(this.getNbtId()));
		return this;
	}

	@Override
	public DoubleFactionSetting copy() {
		return this.isModdedSetting()
				? new DoubleFactionSetting(getDefaultValue(), getNbtId(), getDisplayName(), true, getModId(), getStep(), getMin(), getMax(), getLore())
				: new DoubleFactionSetting(getDefaultValue(), getNbtId(), getDisplayName(), getStep(), getMin(), getMax(), getLore());
	}

    @Override
    public void writeBuf(RegistryFriendlyByteBuf buf) {
        buf.writeDouble(this.get());
    }

    @Override
	public void increment() {
		set(Math.min(get() + 1, Double.MAX_VALUE));
	}

	@Override
	public void decrement() {
		set(Math.max(get() - 1, Double.MIN_VALUE));
	}


}
