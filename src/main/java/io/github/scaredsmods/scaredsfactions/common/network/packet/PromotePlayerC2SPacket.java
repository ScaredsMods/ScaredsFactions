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

import java.util.Arrays;
import java.util.UUID;

public record PromotePlayerC2SPacket(UUID targetUUID) implements CustomPacketPayload {

    public static final Type<PromotePlayerC2SPacket> TYPE = new Type<>(ScaredsFactionMod.id("promote_player_c2s"));
    public static final StreamCodec<ByteBuf, PromotePlayerC2SPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PromotePlayerC2SPacket::targetUUID,
            PromotePlayerC2SPacket::new
    );

    public static void handle(PromotePlayerC2SPacket packet, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
        Faction faction = data.getFactionFromPlayer(player.getUUID());
        if (faction == null) return;

        Faction.Rank playerRank = faction.getMembers().get(player.getUUID());
        if (!faction.isMember(packet.targetUUID)) return;
        Faction.Rank targetRank = faction.getMembers().get(packet.targetUUID);

        int newRankId = targetRank.getId() + 1;
        if (newRankId < 0) return;

        Faction.Rank newRank = Faction.Rank.getRankById(newRankId);
        if (!Arrays.asList(playerRank.getManageableRanks()).contains(targetRank)) return;
        if (!Arrays.asList(playerRank.getManageableRanks()).contains(newRank)) return;

        faction.setRank(packet.targetUUID, newRank);
        data.save(player.serverLevel());
    }
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
