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

import io.github.scaredsmods.scaredsfactions.api.server.network.packet.IAbstractFactionPacket
import io.github.scaredsmods.scaredsfactions.api.server.network.packet.nullable.NullableUUIDPacket
import io.github.scaredsmods.scaredsfactions.common.faction.Faction
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.NetworkEvent
import java.util.*
import java.util.function.Supplier

class PendingOwnershipTransferC2SPacket(private val target: UUID?) : NullableUUIDPacket<PendingOwnershipTransferC2SPacket>(target) {

	override fun handle(packet: PendingOwnershipTransferC2SPacket, ctx: Supplier<NetworkEvent.Context>) {
		ctx.get().enqueueWork {
			val player : ServerPlayer = ctx.get().sender ?: return@enqueueWork
			val data : FactionSavedData = FactionSavedData.getSavedData(player.serverLevel());
			val faction : Faction = data.getFactionFromPlayer(player.uuid) ?: return@enqueueWork
			faction.pendingTransfer = target
			data.save(player.serverLevel())
		}
		ctx.get().packetHandled = true
	}

	companion object : IAbstractFactionPacket.Decoder<PendingOwnershipTransferC2SPacket> {
		override fun decode(buf: FriendlyByteBuf): PendingOwnershipTransferC2SPacket {
			return PendingOwnershipTransferC2SPacket(buf.readNullable {b -> b.readUUID()})
		}

	}
}
