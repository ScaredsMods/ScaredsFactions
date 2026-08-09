package io.github.scaredsmods.scaredsfactions.common.network.packet;

import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData;
import io.github.scaredsmods.scaredsfactions.common.util.MessageUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RenameFactionC2SPacket(String newName) implements CustomPacketPayload {

    public static final Type<RenameFactionC2SPacket> TYPE = new Type<>(ScaredsFactionMod.id("rename_faction_c2s"));
    public static final StreamCodec<ByteBuf, RenameFactionC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RenameFactionC2SPacket::newName,
            RenameFactionC2SPacket::new
    );

    public static void handle(RenameFactionC2SPacket packet, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
        Faction faction = data.getFactionFromPlayer(player.getUUID());
        if (faction == null) return;
        if (data.factionExists(packet.newName)) {
            player.sendSystemMessage(MessageUtil.Prefix.error("This faction already exists! Please choose another name!"));
            return;
        }
        if (!faction.getOwner().equals(player.getUUID())) {
            player.sendSystemMessage(MessageUtil.Prefix.error("You are not the owner of the faction!"));
            return;
        }

        data.renameFaction(faction, packet.newName);
        data.save(player.serverLevel());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
