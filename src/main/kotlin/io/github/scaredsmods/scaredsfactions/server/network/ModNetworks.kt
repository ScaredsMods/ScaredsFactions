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
package io.github.scaredsmods.scaredsfactions.server.network;

import io.github.scaredsmods.scaredsfactions.ScaredsFactionMod
import io.github.scaredsmods.scaredsfactions.server.network.packet.*
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.simple.SimpleChannel

object ModNetworks {


	private const val PROTOCOL_VERSION: String = "1.0.0"
	@JvmField
	val CHANNEL: SimpleChannel = NetworkRegistry.newSimpleChannel(
		ScaredsFactionMod.id("main"),
		{ PROTOCOL_VERSION },
		{ it == PROTOCOL_VERSION },
		{ it == PROTOCOL_VERSION }
	)

	private var id = 0


	@JvmStatic
	fun register() {
		register(RenameFactionPacket::class.java, RenameFactionPacket)
		register(TransferOwnershipPacket::class.java, TransferOwnershipPacket)
		register(PromotePlayerPacket::class.java, PromotePlayerPacket)
		register(DemotePlayerPacket::class.java, DemotePlayerPacket)
		register(UpdateFactionSettingsPacket::class.java, UpdateFactionSettingsPacket)
		register(OpenScreenS2CPacket::class.java, OpenScreenS2CPacket)
	}

	@Suppress("INFERRED_INVISIBLE_RETURN_TYPE_WARNING")
	fun <T : AbstractFactionPacket<T>> register(clazz: Class<T>, decoder: AbstractFactionPacket.Decoder<T>) {
		CHANNEL.registerMessage(
			++id,
			clazz,
			{ packet: T, buf: FriendlyByteBuf -> packet.encode(packet, buf) },
			{ buf : FriendlyByteBuf -> decoder.decode(buf) },
			{ packet, ctx -> packet.handle(packet, ctx) }
		)
	}
}
