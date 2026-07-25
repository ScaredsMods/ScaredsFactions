package io.github.scaredsmods.scaredsfactions.api.server.network.packet

import net.minecraft.network.FriendlyByteBuf

abstract class MapPacket<A, B, T : MapPacket<A, B, T>>(val first: A, val second: B, val encodeFirst: (FriendlyByteBuf, A) -> Unit, val encodeSecond: (FriendlyByteBuf, B) -> Unit)  : IAbstractFactionPacket<T> {

    override fun encode(packet: T, buf: FriendlyByteBuf) {
        encodeFirst(buf, packet.first)
        encodeSecond(buf, packet.second)
    }
}