package io.github.scaredsmods.scaredsfactions.common.network.packet;

import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSettings;
import io.github.scaredsmods.scaredsfactions.common.util.FactionUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record TransferOwnershipC2SPacket(UUID target) implements CustomPacketPayload {

    public static final Type<TransferOwnershipC2SPacket> TYPE = new Type<>(ScaredsFactionMod.id("transfer_ownership_c2s"));
    public static final StreamCodec<ByteBuf, TransferOwnershipC2SPacket> STREAM_CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, TransferOwnershipC2SPacket::target, TransferOwnershipC2SPacket::new);

    public static void handle(TransferOwnershipC2SPacket packet, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
        Faction faction = data.getFactionFromPlayer(player.getUUID());
        if (faction == null) return;
        if (faction.getOwner() != player.getUUID()) return;
        if (!faction.isMember(packet.target)) return;
        UUID oldOwner = faction.getOwner();
        faction.setRank(oldOwner, Faction.Rank.FIELD_MARSHAL);

        Faction.Rank newOwnerRank = faction.getSettingValue(FactionSettings.OWNER_RANK.getNbtId(), FactionUtil.<Faction.Rank>enumSetting());
        faction.setOwner(packet.target, newOwnerRank);
        faction.setRank(packet.target, newOwnerRank);
        data.save(player.serverLevel());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
