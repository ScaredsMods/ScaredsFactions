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
import io.github.scaredsmods.scaredsfactions.api.server.network.packet.StringPacket
import io.github.scaredsmods.scaredsfactions.client.screen.menu.EditStringSettingMenu
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.world.SimpleMenuProvider
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.NetworkHooks
import java.util.function.Supplier

class OpenEditStringSettingC2SPacket(private val nbtId: String) : StringPacket<OpenEditStringSettingC2SPacket>(nbtId) {

	override fun handle(packet: OpenEditStringSettingC2SPacket, ctx: Supplier<NetworkEvent.Context>) {
		ctx.get().enqueueWork {
			val player = ctx.get().sender ?: return@enqueueWork
			NetworkHooks.openScreen(player, SimpleMenuProvider(
				{ containerId, inv, _ -> EditStringSettingMenu(containerId, inv, packet.nbtId) },
				Component.literal("Edit Setting")
			)
			) { buf -> buf.writeUtf(packet.nbtId) }
		}
		ctx.get().packetHandled = true
	}

	companion object : IAbstractFactionPacket.Decoder<OpenEditStringSettingC2SPacket> {
		override fun decode(buf: FriendlyByteBuf): OpenEditStringSettingC2SPacket {
			return OpenEditStringSettingC2SPacket(buf.readUtf())
		}

	}


}
