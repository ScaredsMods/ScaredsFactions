package io.github.scaredsmods.scaredsfactions.common.network.packet;

import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.common.component.ModDataComponents;
import io.github.scaredsmods.scaredsfactions.common.component.RespawnBeaconDataComponent;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData;
import io.github.scaredsmods.scaredsfactions.common.util.MessageUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record ResetBeaconPosC2SPacket() implements CustomPacketPayload {

    public static final Type<ResetBeaconPosC2SPacket> TYPE = new Type<>(ScaredsFactionMod.id("reset_beacon_pos_c2s"));
    public static final StreamCodec<ByteBuf, ResetBeaconPosC2SPacket> STREAM_CODEC = StreamCodec.unit(new ResetBeaconPosC2SPacket());

    public static void handle(ResetBeaconPosC2SPacket packet, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
        Faction faction = data.getFactionFromPlayer(player.getUUID());
        if (faction == null) return;
        if (!faction.hasBeacon()) {
            player.sendSystemMessage(MessageUtil.Prefix.error("You must have a beacon to do this!"));
            return;
        }

        player.serverLevel().destroyBlock(faction.getBeaconPos(), false);
        faction.removeBeacon();
        ItemStack beacon = new ItemStack(Items.BEACON);
        beacon.set(ModDataComponents.RESPAWN_BEACON.get(), new RespawnBeaconDataComponent(true));
        beacon.set(DataComponents.ITEM_NAME, Component.literal("Respawn Beacon")
                .withStyle(style -> style.withBold(true)
                    .withColor(ChatFormatting.RED)
                    .withItalic(false)));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("This is your faction's respawn beacon!").withStyle(style -> style
                .withColor(ChatFormatting.GRAY)
                .withItalic(false)));
        lore.add(Component.literal("It functions as your bed, and lifeline!")
                .withStyle(style -> style
                        .withColor(ChatFormatting.GRAY)
                        .withItalic(false)));
        lore.add(Component.literal("Hide it well: If other factions get a hold of it, you can no longer respawn!")
                .withStyle(style -> style
                        .withBold(false)
                        .withItalic(false)
                        .withColor(ChatFormatting.GRAY)));

        beacon.set(DataComponents.LORE, new ItemLore(lore));
        player.getInventory().add(beacon);
        data.save(player.serverLevel());
        player.getServer().saveAllChunks(false, true, false);
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
