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
import io.github.scaredsmods.scaredsfactions.api.server.network.packet.MapPacket
import io.github.scaredsmods.scaredsfactions.common.faction.ClientFactionSavedData
import io.github.scaredsmods.scaredsfactions.common.faction.Faction
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.network.NetworkEvent
import java.util.UUID
import java.util.function.Supplier

class SyncFactionDataS2CPacket(val factions: Map<String, Faction>) : IAbstractFactionPacket<SyncFactionDataS2CPacket> {

	override fun encode(packet: SyncFactionDataS2CPacket, buf: FriendlyByteBuf) {
		buf.writeMap(packet.factions, FriendlyByteBuf::writeUtf) { b, f -> f.write(b) }
	}

	override fun handle(packet: SyncFactionDataS2CPacket, ctx: Supplier<NetworkEvent.Context>) {
		ctx.get().enqueueWork {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT) {
				Runnable {
					ClientFactionSavedData.putAll(packet.factions)
				}
			}
		}
		ctx.get().packetHandled = true
	}

	companion object : IAbstractFactionPacket.Decoder<SyncFactionDataS2CPacket> {
		override fun decode(buf: FriendlyByteBuf): SyncFactionDataS2CPacket {
			return SyncFactionDataS2CPacket(buf.readMap({ b -> b.readUtf() }, { b -> Faction.read(b) }))
		}
	}
}
