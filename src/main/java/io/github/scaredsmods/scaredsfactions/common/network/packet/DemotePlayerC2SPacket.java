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

public record DemotePlayerC2SPacket(UUID targetUUID) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DemotePlayerC2SPacket> TYPE = new CustomPacketPayload.Type<>(ScaredsFactionMod.id("demote_player_c2s"));

    public static final StreamCodec<ByteBuf, DemotePlayerC2SPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            DemotePlayerC2SPacket::targetUUID,
            DemotePlayerC2SPacket::new
    );

    public static void handle(DemotePlayerC2SPacket packet, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
        Faction faction = data.getFactionFromPlayer(player.getUUID());
        if (faction == null) return;
        if (!faction.isMember(packet.targetUUID)) return;

        Faction.Rank playerRank = faction.getMembers().get(player.getUUID());
        Faction.Rank targetRank = faction.getMembers().get(packet.targetUUID);
        int newRankId = targetRank.getId() - 1;
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
