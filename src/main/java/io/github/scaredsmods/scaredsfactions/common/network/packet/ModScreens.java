package io.github.scaredsmods.scaredsfactions.common.network.packet;

import com.mojang.authlib.GameProfile;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.AbstractFactionSetting;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.BooleanFactionSetting;
import io.github.scaredsmods.scaredsfactions.client.screen.menu.*;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.IntFunction;

public enum ModScreens {


    MANAGE_FACTION(0) {
        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, ServerPlayer player) {
            return new ManageFactionMenu(id, inv);
        }
    },
    RENAME_FACTION(1) {
        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, ServerPlayer player) {
            return new RenameFactionMenu(id, inv);
        }
    },
    TRANSFER_OWNERSHIP(2) {
        @Override
        public void writeBuf(ServerPlayer player, RegistryFriendlyByteBuf buf) {
            FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
            Faction faction = data.getFactionFromPlayer(player.getUUID());
            if (faction == null) return;

            Map<GameProfile, Faction.Rank> profileMembers = buildProfileMap(faction, player);
            buf.writeInt(profileMembers.size());
            profileMembers.forEach((profile, rank) -> {
                ByteBufCodecs.GAME_PROFILE.encode(buf, profile);
                buf.writeById(Faction.Rank::ordinal, rank);
            });
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, ServerPlayer player) {
            FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
            Faction faction = data.getFactionFromPlayer(player.getUUID());
            if (faction == null) throw new IllegalStateException("Player is not in a faction");

            Map<GameProfile, Faction.Rank> profileMembers = buildProfileMap(faction, player);
            return new TransferOwnershipMenu(id, inv, profileMembers);
        }
    },
    VIEW_MEMBERS(3) {
        @Override
        public void writeBuf(ServerPlayer player, RegistryFriendlyByteBuf buf) {
            FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
            Faction faction = data.getFactionFromPlayer(player.getUUID());
            if (faction == null) return;

            Map<GameProfile, Faction.Rank> profileMembers = buildProfileMap(faction, player);
            buf.writeInt(profileMembers.size());
            profileMembers.forEach((profile, rank) -> {
                ByteBufCodecs.GAME_PROFILE.encode(buf, profile);
                buf.writeById(Faction.Rank::ordinal, rank);
            });
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, ServerPlayer player) {
            FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
            Faction faction = data.getFactionFromPlayer(player.getUUID());
            if (faction == null) throw new IllegalStateException("Player is not in a faction");

            Map<GameProfile, Faction.Rank> profileMembers = buildProfileMap(faction, player);
            return new ViewMembersMenu(id, inv, profileMembers);
        }
    },
    FACTION_SETTINGS(4) {
        @Override
        public void writeBuf(ServerPlayer player, RegistryFriendlyByteBuf buf) {
            FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
            Faction faction = data.getFactionFromPlayer(player.getUUID());
            if (faction == null) return;

            for (AbstractFactionSetting<?, ?> setting : faction.getSettings()) {
                setting.writeBuf(buf);
            }
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, ServerPlayer player) {
            FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
            Faction faction = data.getFactionFromPlayer(player.getUUID());
            if (faction == null) throw new IllegalStateException("Player is not in a faction");

            return new FactionSettingsMenu(id, inv, faction.getSettings());
        }
    },
    CONFIRM_TRANSFER(5) {
        @Override
        public void writeBuf(ServerPlayer player, RegistryFriendlyByteBuf buf) {
            FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
            Faction faction = data.getFactionFromPlayer(player.getUUID());
            if (faction == null) return;

            buf.writeUUID(faction.getPendingTransfer());
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, ServerPlayer player) {
            FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
            Faction faction = data.getFactionFromPlayer(player.getUUID());
            if (faction == null) throw new IllegalStateException("Player is not in a faction");

            return new ConfirmTransferOwnershipMenu(id, inv, faction.getPendingTransfer());
        }
    },
    CONFIRM_RESET_BEACON(6) {
        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, ServerPlayer player) {
            return new ConfirmResetBeaconPosMenu(id, inv);
        }
    },
    CLOSE(7) {
        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, ServerPlayer player) {
            throw new UnsupportedOperationException("Close does not open any screen!");
        }
    };

    private final int id;

    ModScreens(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Component getTitle() {
        return Component.literal(this.name().replace("_", " "));
    }

    public abstract AbstractContainerMenu createMenu(int id, Inventory inv, ServerPlayer player);

    public void writeBuf(ServerPlayer player, RegistryFriendlyByteBuf buf) {}

    public static Map<GameProfile, Faction.Rank> buildProfileMap(Faction faction, ServerPlayer player) {
        Map<GameProfile, Faction.Rank> result = new HashMap<>();

        for (Map.Entry<UUID, Faction.Rank> entry : faction.getMembers().entrySet()) {
            UUID uuid = entry.getKey();
            Faction.Rank rank = entry.getValue();

            ServerPlayer onlineMember = player.getServer().getPlayerList().getPlayer(uuid);
            GameProfile profile;

            if (onlineMember != null) {
                profile = onlineMember.getGameProfile();
            } else {

                profile = Objects.requireNonNull(player.getServer().getProfileCache())
                        .get(uuid)
                        .orElse(new GameProfile(uuid, "Unknown"));
            }

            result.put(profile, rank);
        }
        return result;
    }

    public static final IntFunction<ModScreens> BY_ID =
            ByIdMap.continuous(
                    ModScreens::getId,
                    ModScreens.values(),
                    ByIdMap.OutOfBoundsStrategy.ZERO
            );

    public static final StreamCodec<ByteBuf, ModScreens> STREAM_CODEC =
            ByteBufCodecs.idMapper(ModScreens.BY_ID, ModScreens::getId);
}