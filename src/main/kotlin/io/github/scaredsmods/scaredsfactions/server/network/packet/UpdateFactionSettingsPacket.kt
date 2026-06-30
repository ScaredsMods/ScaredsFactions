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
package io.github.scaredsmods.scaredsfactions.server.network.packet

import io.github.scaredsmods.scaredsfactions.faction.Faction.FactionSavedData
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier


class UpdateFactionSettingsPacket(private val nbtId: String, private val value : CompoundTag?) : AbstractFactionPacket<UpdateFactionSettingsPacket> {
	override fun encode(packet: UpdateFactionSettingsPacket, buf: FriendlyByteBuf) {
		buf.writeUtf(packet.nbtId)
		buf.writeNbt(packet.value)
	}

	override fun handle(packet: UpdateFactionSettingsPacket, ctx: Supplier<NetworkEvent.Context>) {
		ctx.get().enqueueWork {
			val player = ctx.get().getSender() ?: return@enqueueWork
			val data = FactionSavedData.getSavedData(player.serverLevel())
			val faction = data.getFactionFromPlayer(player.getUUID()) ?: return@enqueueWork
			if (faction.owner != player.getUUID()) return@enqueueWork

			for (setting in faction.settings) {
				if (setting.nbtId == packet.nbtId) {
					setting.load(packet.value)
					break
				}
			}
			data.setDirty()
		}
		ctx.get().packetHandled = true
	}

	companion object : AbstractFactionPacket.Decoder<UpdateFactionSettingsPacket> {
		override fun decode(buf: FriendlyByteBuf): UpdateFactionSettingsPacket {
			return UpdateFactionSettingsPacket(buf.readUtf(), buf.readNbt())
		}
	}
}
