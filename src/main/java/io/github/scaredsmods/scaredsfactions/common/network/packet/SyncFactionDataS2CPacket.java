package io.github.scaredsmods.scaredsfactions.common.network.packet;

import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.common.faction.ClientFactionSavedData;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record SyncFactionDataS2CPacket(Map<String, Faction> factions) implements CustomPacketPayload {

    public static final Type<SyncFactionDataS2CPacket> TYPE = new Type<>(ScaredsFactionMod.id("sync_faction_data_s2c"));

    private static final StreamCodec<FriendlyByteBuf, Map<String, Faction>> MAP_CODEC =
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, Faction.STREAM_CODEC);

    public static final StreamCodec<FriendlyByteBuf, SyncFactionDataS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> MAP_CODEC.encode(buf, packet.factions()),
            buf -> new SyncFactionDataS2CPacket(MAP_CODEC.decode(buf))
    );

    public static void handle(SyncFactionDataS2CPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientFactionSavedData.putAll(packet.factions()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
