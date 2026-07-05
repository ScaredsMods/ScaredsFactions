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
import net.minecraft.network.chat.Component;

public abstract class NumericFactionSetting<N extends Number, T extends NumericFactionSetting<N, T>> extends AbstractFactionSetting<N, T> {


	private final N step;
	private final N min;
	private final N max;

	public NumericFactionSetting(N defaultValue, String nbtId, String displayName, N step, N min, N max, String... lore) {
		super(defaultValue, nbtId, displayName, lore);
		this.step = step;
		this.min = min;
		this.max = max;
	}

	public NumericFactionSetting(N defaultValue, String nbtId, String displayName, boolean isModdedSetting, String modId, N step, N min, N max, String... lore) {
		super(defaultValue, nbtId, displayName, isModdedSetting, modId, lore);
		this.step = step;
		this.min = min;
		this.max = max;
	}

	public abstract void increment();
	public abstract void decrement();

	@Override
	public void onClick(int mouseButton, Runnable sendUpdate) {
		if (mouseButton == 0) increment();
		else if (mouseButton == 1) decrement();
		sendUpdate.run();
	}

	public N getStep() {
		return step;
	}

	public N getMin() {
		return min;
	}

	public N getMax() {
		return max;
	}

	@Override
	public Component getCurrentValueAsComponent() {
		return Component.literal(get().toString())
				.withStyle(style -> style
						.withColor(ChatFormatting.YELLOW)
						.withItalic(false)
				);
	}
}
