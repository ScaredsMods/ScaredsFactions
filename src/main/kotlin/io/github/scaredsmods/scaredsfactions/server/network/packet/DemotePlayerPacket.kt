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
import io.github.scaredsmods.scaredsfactions.api.server.network.packet.UUIDPacket
import io.github.scaredsmods.scaredsfactions.common.faction.Faction
import io.github.scaredsmods.scaredsfactions.common.faction.Faction.Rank
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.NetworkEvent
import java.util.*
import java.util.function.Supplier

class DemotePlayerPacket(private val targetUUID: UUID) : UUIDPacket<DemotePlayerPacket>(targetUUID) {

	companion object : IAbstractFactionPacket.Decoder<DemotePlayerPacket> {
		override fun decode(buf: FriendlyByteBuf): DemotePlayerPacket {
			return DemotePlayerPacket(buf.readUUID())
		}
	}

	override fun handle(packet: DemotePlayerPacket, ctx: Supplier<NetworkEvent.Context>) {
		ctx.get().enqueueWork {
			val player : ServerPlayer = ctx.get().sender ?: return@enqueueWork
			val data : FactionSavedData = FactionSavedData.getSavedData(player.serverLevel())
			val faction : Faction = data.getFactionFromPlayer(player.uuid)
			if (!faction.members.containsKey(packet.targetUUID)) return@enqueueWork

			val playerRank : Rank? = faction.members[player.uuid]
			val targetRank : Rank? = faction.members[packet.targetUUID]

			val newRankId : Int = targetRank!!.id - 1;
			if (newRankId < 0) return@enqueueWork

			val newRank : Rank? = Rank.getRankById(newRankId)
			if (!listOf<Rank?>(*playerRank!!.manageableRanks).contains(targetRank)) return@enqueueWork
			if (!listOf(*playerRank.manageableRanks).contains(newRank)) return@enqueueWork

			faction.members[packet.targetUUID] = newRank
			data.save(player.serverLevel())
		}

		ctx.get().packetHandled = true
	}
}
