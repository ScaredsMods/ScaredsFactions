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

import io.github.scaredsmods.scaredsfactions.common.network.packet.OpenEditStringSettingC2SPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class StringFactionSetting extends AbstractFactionSetting<String, StringFactionSetting> {

	public StringFactionSetting(String defaultValue, String nbtId, String displayName, String... lore) {
		super(defaultValue, nbtId, displayName, lore);
	}

	public StringFactionSetting(String defaultValue, String nbtId, String displayName, boolean isModdedSetting, String modId, String... lore) {
		super(defaultValue, nbtId, displayName, isModdedSetting, modId, lore);
	}

	@Override
	public void save(CompoundTag tag) {
		tag.putString(getNbtId(), get());
	}

	@Override
	public StringFactionSetting load(CompoundTag tag) {
		this.set(tag.getString(getNbtId()));
		return this;
	}

	@Override
	public StringFactionSetting copy() {
		return this.isModdedSetting() ? new StringFactionSetting(getDefaultValue(), getNbtId(), getDisplayName(), true, getModId(), getLore()) : new StringFactionSetting(getDefaultValue(), getNbtId(), getDisplayName(), getLore());
	}

	@Override
	public void onClick(int mouseButton, Runnable sendUpdate) {
		PacketDistributor.sendToServer(new OpenEditStringSettingC2SPacket(getNbtId()));
	}

	@Override
	public Component getCurrentValueAsComponent() {
		return Component.literal(get())
				.withStyle(style -> style
					.withColor(ChatFormatting.YELLOW)
					.withItalic(false)
				);
	}

    @Override
    public void writeBuf(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(this.get());
    }

}
