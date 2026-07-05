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
package io.github.scaredsmods.scaredsfactions.common.util;

import io.github.scaredsmods.scaredsfactions.server.network.ModNetworks;
import io.github.scaredsmods.scaredsfactions.api.server.network.packet.IAbstractFactionPacket;

public class PacketUtil {


	public static <T extends IAbstractFactionPacket<T>> void registerMessage(int id, Class<T> clazz, IAbstractFactionPacket.Decoder<T> decoder) {
		ModNetworks.CHANNEL.registerMessage(id, clazz,
				(packet, buf) -> packet.encode(packet, buf), decoder::decode,
				(packet, ctx) -> packet.handle(packet, ctx));
	}
}
