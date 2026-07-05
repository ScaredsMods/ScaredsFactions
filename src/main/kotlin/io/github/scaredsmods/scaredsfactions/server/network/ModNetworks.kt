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

import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod
import io.github.scaredsmods.scaredsfactions.server.network.packet.*
import io.github.scaredsmods.scaredsfactions.common.util.PacketUtil.registerMessage
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
	fun init() {
		registerMessage(id, RenameFactionPacket::class.java, RenameFactionPacket)
		registerMessage(++id, TransferOwnershipPacket::class.java, TransferOwnershipPacket)
		registerMessage(++id, PromotePlayerPacket::class.java, PromotePlayerPacket)
		registerMessage(++id, DemotePlayerPacket::class.java, DemotePlayerPacket)
		registerMessage(++id, UpdateFactionSettingsPacket::class.java, UpdateFactionSettingsPacket)
		registerMessage(++id,OpenScreenC2SPacket::class.java, OpenScreenC2SPacket)
		registerMessage(++id,PendingOwnershipTransferC2SPacket::class.java, PendingOwnershipTransferC2SPacket)
		registerMessage(++id,OpenEditStringSettingC2SPacket::class.java, OpenEditStringSettingC2SPacket)
	}



}
