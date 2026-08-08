package io.github.scaredsmods.scaredsfactions.common.network.packet;

import io.github.scaredsmods.scaredsfactions.client.screen.menu.EditStringSettingMenu;
import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenEditStringSettingC2SPacket(String nbtId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenEditStringSettingC2SPacket> TYPE = new CustomPacketPayload.Type<>(ScaredsFactionMod.id("edit_string_setting_c2s"));

    public static final StreamCodec<ByteBuf, OpenEditStringSettingC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            OpenEditStringSettingC2SPacket::nbtId,
            OpenEditStringSettingC2SPacket::new
    );

    public static void handle(OpenEditStringSettingC2SPacket packet, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, player1) -> new EditStringSettingMenu(containerId, playerInventory, packet.nbtId), Component.literal("Edit Setting")));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
