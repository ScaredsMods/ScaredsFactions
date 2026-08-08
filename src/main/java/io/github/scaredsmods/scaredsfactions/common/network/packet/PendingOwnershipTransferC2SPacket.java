package io.github.scaredsmods.scaredsfactions.common.network.packet;

import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record PendingOwnershipTransferC2SPacket(UUID target) implements CustomPacketPayload {

    public static final Type<PendingOwnershipTransferC2SPacket> TYPE = new Type<>(ScaredsFactionMod.id("pending_ownership_c2s"));
    public static final StreamCodec<ByteBuf, PendingOwnershipTransferC2SPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PendingOwnershipTransferC2SPacket::target,
            PendingOwnershipTransferC2SPacket::new
    );

    public static void handle(PendingOwnershipTransferC2SPacket packet, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
        Faction faction = data.getFactionFromPlayer(player.getUUID());
        if (faction == null) return;
        faction.setPendingTransfer(packet.target);
        data.save(player.serverLevel());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
