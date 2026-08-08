package io.github.scaredsmods.scaredsfactions.common.network.packet;

import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.AbstractFactionSetting;
import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSettings;
import io.github.scaredsmods.scaredsfactions.common.util.FactionUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateFactionSettingC2SPacket(String nbtId, CompoundTag value) implements CustomPacketPayload {

    public static final Type<UpdateFactionSettingC2SPacket> TYPE = new Type<>(ScaredsFactionMod.id("update_faction_setting_c2s"));
    public static final StreamCodec<ByteBuf, UpdateFactionSettingC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, UpdateFactionSettingC2SPacket::nbtId,
            ByteBufCodecs.COMPOUND_TAG, UpdateFactionSettingC2SPacket::value,
            UpdateFactionSettingC2SPacket::new
    );

    public static void handle(UpdateFactionSettingC2SPacket packet, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
        Faction faction = data.getFactionFromPlayer(player.getUUID());
        if (faction == null) return;
        if (!faction.getOwner().equals(player.getUUID())) return;
        for (AbstractFactionSetting<?,?> setting : faction.getSettings()) {
            if (setting.getNbtId().equals(packet.nbtId)) {
                setting.load(packet.value);
                break;
            }
        }
        if (packet.nbtId.equals(FactionSettings.OWNER_RANK.getNbtId())) {
            Faction.Rank newRank = faction.getSettingValue(FactionSettings.OWNER_RANK.getNbtId(), FactionUtil.<Faction.Rank>enumSetting());
            faction.setRank(faction.getOwner(), newRank);
        }
        data.save(player.serverLevel());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
