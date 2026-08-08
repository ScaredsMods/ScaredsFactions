package io.github.scaredsmods.scaredsfactions.common.network.packet;

import com.mojang.authlib.GameProfile;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.AbstractFactionSetting;
import io.github.scaredsmods.scaredsfactions.client.screen.menu.*;
import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData;
import io.github.scaredsmods.scaredsfactions.common.util.MessageUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;
import java.util.stream.Collectors;

public record OpenScreenC2SPacket(ModScreens screen, Component title) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenScreenC2SPacket> TYPE = new Type<>(ScaredsFactionMod.id("open_screen_c2s"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreenC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ModScreens.STREAM_CODEC, OpenScreenC2SPacket::screen,
            ComponentSerialization.STREAM_CODEC, OpenScreenC2SPacket::title,
            OpenScreenC2SPacket::new
    );

    public static void handle(OpenScreenC2SPacket packet, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
        Faction faction = data.getFactionFromPlayer(player.getUUID());
        if (faction == null) return;
        switch (packet.screen) {
            case CLOSE -> {
                player.closeContainer();
            }
            case MANAGE_FACTION -> {
                player.openMenu(new SimpleMenuProvider((containerId, playerInventory, player1) -> new ManageFactionMenu(containerId, playerInventory), packet.title));
            }
            case RENAME_FACTION -> {
                player.openMenu(new SimpleMenuProvider((containerId, playerInventory, player1) -> new RenameFactionMenu(containerId, playerInventory), packet.title));
            }
            case CONFIRM_TRANSFER -> {
                UUID targetUUID = faction.getPendingTransfer();
                if (targetUUID == null) return;
                player.openMenu(new SimpleMenuProvider((containerId, playerInventory, player1) -> new ConfirmTransferOwnershipMenu(containerId, playerInventory, targetUUID), packet.title), buf -> buf.writeUUID(targetUUID));
            }
            case CONFIRM_RESET_BEACON -> {
                if (!faction.hasBeacon()) {
                    player.sendSystemMessage(MessageUtil.Prefix.error("You must have a beacon to do this!"));
                    return;
                }
                player.openMenu(new SimpleMenuProvider((containerId, playerInventory, player1) -> new ConfirmResetBeaconPosMenu(containerId, playerInventory), packet.title));
            }
            case FACTION_SETTINGS -> player.openMenu(new SimpleMenuProvider((containerId, playerInventory, player1) -> new FactionSettingsMenu(containerId, playerInventory, faction.getSettings()), packet.title),
                    buf -> {
                            buf.writeVarInt(faction.getSettings().size());
                            for (AbstractFactionSetting<? , ?> setting : faction.getSettings()) {
                                buf.writeUtf(setting.getNbtId());
                                CompoundTag tag = new CompoundTag();
                                setting.save(tag);
                                buf.writeNbt(tag);
                            }
                    }
            );
            case TRANSFER_OWNERSHIP -> {
                Map<GameProfile, Faction.Rank> profileMembers = new HashMap<>();
                for (Map.Entry<UUID, Faction.Rank> entry : faction.getMembers().entrySet()) {
                    ServerPlayer onlineMember = Objects.requireNonNull(player.getServer()).getPlayerList().getPlayer(entry.getKey());
                    GameProfile profile = onlineMember != null ? onlineMember.getGameProfile()
                            : player.getServer().getProfileCache().get(entry.getKey()).orElse(new GameProfile(entry.getKey(), "Unknown"));
                    profileMembers.put(profile, entry.getValue());
                }

                Map<GameProfile, Faction.Rank> filteredMembers = profileMembers.entrySet().stream()
                        .filter(entry -> entry.getValue() == Faction.Rank.FIELD_MARSHAL)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                player.openMenu(new SimpleMenuProvider((containerId, playerInventory, player1) -> new TransferOwnershipMenu(containerId, playerInventory, filteredMembers), packet.title), buf -> {
                    buf.writeInt(faction.getSettings().size());
                    for (Map.Entry<GameProfile, Faction.Rank> entry : filteredMembers.entrySet()) {
                        ByteBufCodecs.GAME_PROFILE.encode(buf, entry.getKey());
                        buf.writeEnum(entry.getValue());
                    }
                });
            }
            case VIEW_MEMBERS -> {
                Map<GameProfile, Faction.Rank> profileMembers = new LinkedHashMap<>();

                List<Map.Entry<UUID, Faction.Rank>> sortedMembers = faction.getMembers().entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(Faction.Rank::getId).reversed().thenComparing(Faction.Rank::getName)))
                        .toList();
                for (Map.Entry<UUID, Faction.Rank> entry : sortedMembers) {
                    ServerPlayer onlineMember = Objects.requireNonNull(player.getServer()).getPlayerList().getPlayer(entry.getKey());
                    GameProfile profile = onlineMember != null ? onlineMember.getGameProfile()
                            : player.getServer().getProfileCache().get(entry.getKey()).orElse(new GameProfile(entry.getKey(), "Unknown"));
                    profileMembers.put(profile, entry.getValue());
                }
                player.openMenu(new SimpleMenuProvider((containerId, playerInventory, player1) -> new TransferOwnershipMenu(containerId, playerInventory, profileMembers), packet.title), buf -> {
                    buf.writeInt(profileMembers.size());
                    for (Map.Entry<GameProfile, Faction.Rank> entry : profileMembers.entrySet()) {
                        ByteBufCodecs.GAME_PROFILE.encode(buf, entry.getKey());
                        buf.writeEnum(entry.getValue());
                    }
                });
            }
        }
    }

    @Override
    public Type<OpenScreenC2SPacket> type() {
        return TYPE;
    }
}
