/*
*  Copyright (C) 2026 ScaredRabbitNL
*
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with this program. If not, see <https://www.gnu.org/licenses/>.
*/
package io.github.scaredsmods.scaredsfactions.common.event;

import com.mojang.authlib.GameProfile;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.BooleanFactionSetting;
import io.github.scaredsmods.scaredsfactions.common.FactionMod;
import io.github.scaredsmods.scaredsfactions.common.ModConfigs;
import io.github.scaredsmods.scaredsfactions.common.ModTags;
import io.github.scaredsmods.scaredsfactions.common.ModTranslations;
import io.github.scaredsmods.scaredsfactions.common.command.FactionCommand;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSettings;
import io.github.scaredsmods.scaredsfactions.common.util.MessageUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = FactionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		FactionCommand.register(event.getDispatcher());
	}

	@SubscribeEvent
	public static void preventVanillaPvp(LivingHurtEvent event) {
		if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;
		if (!(event.getEntity() instanceof ServerPlayer victim)) return;

		FactionSavedData data = FactionSavedData.getSavedData(attacker.serverLevel());
		List<ResourceKey<DamageType>> blackListedTypes = List.of(DamageTypes.ARROW,  DamageTypes.TRIDENT, DamageTypes.PLAYER_ATTACK);

		boolean isVanillaItem = blackListedTypes.stream().anyMatch(type -> event.getSource().is(type));
		boolean isDirectPlayerAttack = event.getSource().getDirectEntity() instanceof ServerPlayer;
		if (!isVanillaItem && !isDirectPlayerAttack) return;

		Faction attackerFaction = data.getFactionFromPlayer(attacker.getUUID());
		Faction victimFaction = data.getFactionFromPlayer(victim.getUUID());
		if (attackerFaction == null || victimFaction == null) return;
		if (!attackerFaction.getName().equals(victimFaction.getName())) return;

		boolean setting = victimFaction.getSettingValue(FactionSettings.VANILLA_FRIENDLY_FIRE.getNbtId(), BooleanFactionSetting.class);
		boolean isVanillaPvpEnabled = ModConfigs.commonConfig.factionSettingOverrides.doOverrideEnableVanillaFriendlyFire.get() ? ModConfigs.commonConfig.factionSettingOverrides.overrideEnableVanillaFriendlyFire.get() :
				setting;

		if (isVanillaPvpEnabled) return;
		event.setCanceled(true);
	}


	@SubscribeEvent
	public static void onBeaconInteract(PlayerInteractEvent.RightClickBlock event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (event.getLevel().getBlockState(event.getPos()).getBlock() != Blocks.BEACON) return;
		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		String factionName = data.getFactionByBeaconPosition(event.getPos());
		if (factionName == null) return;
		event.setCanceled(true);
	}


	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (event.isEndConquered()) return;
		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());
		if (faction == null) return;
		if (!data.isHardcored(faction.getName())) {
			return;
		}

		faction.eliminatePlayer(player.getUUID());
		data.save(player.serverLevel());
		player.setGameMode(GameType.SPECTATOR);
		player.sendSystemMessage(MessageUtil.Prefix.error(ModTranslations.BEACON_DESTROYED_ON_RESPAWN));
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;

		GameProfile profile = player.getGameProfile();
		ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
		CompoundTag nbt = new CompoundTag();
		nbt.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), profile));
		stack.setTag(nbt);

		switch (ModConfigs.commonConfig.playerHeadOptions.get()) {
			case ADD_TO_ATTACKER -> {
				attacker.addItem(stack);
			}
			case DROP_AT_GROUND -> player.spawnAtLocation(stack);
			case PLACE_AT_DEATH_LOCATION -> {
				BlockPos playerPos = player.blockPosition();
				ServerLevel level = player.serverLevel();
				level.setBlock(playerPos, Blocks.PLAYER_HEAD.defaultBlockState(), 3);
				BlockEntity blockEntity = level.getBlockEntity(playerPos);
				if (blockEntity instanceof SkullBlockEntity playerHead) {
					playerHead.setOwner(profile);
					playerHead.setChanged();
					level.sendBlockUpdated(playerPos, level.getBlockState(playerPos), level.getBlockState(playerPos), 3);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());
		data.syncToClient(player);
		if (faction == null) return;

		if (!data.isHardcored(faction.getName())) return;
		if (faction.isEliminated(player.getUUID())) {
			player.setGameMode(GameType.SPECTATOR);
			player.sendSystemMessage(MessageUtil.Prefix.info(ModTranslations.BEACON_DESTROYED_ON_JOIN));
			return;
		}

		if (faction.hasBeacon()) {
			player.getServer().execute(() -> {
				if (ModConfigs.commonConfig.respawnPlayerAtFactionBeacon.get()) {
					player.setRespawnPosition(Level.OVERWORLD, faction.getBeaconPos().above(), 0.0F, true, false);
				}
			});
		}

	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (event.getPlacedBlock().getBlock() != Blocks.BEACON) return;

		if (player.getUsedItemHand() != InteractionHand.MAIN_HAND) {
			player.sendSystemMessage(MessageUtil.Prefix.error(ModTranslations.BEACON_NOT_IN_MAIN_HAND_ON_PLACE));
			return;
		}

		ItemStack item = player.getMainHandItem();
		CompoundTag tag = item.getTag();
		if (tag == null || !tag.getBoolean("respawn_beacon")) return;

		if (!(player.serverLevel().dimension().equals(Level.OVERWORLD))) {
			event.setCanceled(true);
			player.sendSystemMessage(MessageUtil.Prefix.error(ModTranslations.BEACON_NOT_IN_OVERWORLD_ON_PLACE));
			return;
		}

		if (player.gameMode.isCreative()) {
			event.setCanceled(true);
			player.sendSystemMessage(MessageUtil.Prefix.error(ModTranslations.PLAYER_NOT_IN_SURVIVAL_ON_PLACE));
			return;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());

		if (faction == null) return;
		if (!faction.getOwner().equals(player.getUUID())) {
			event.setCanceled(true);
			player.sendSystemMessage(MessageUtil.Prefix.error(ModTranslations.INSUFFICIENT_RANK));
			return;
		}
		if (faction.hasBeacon()) {
			event.setCanceled(true);
			player.sendSystemMessage(MessageUtil.Prefix.error(ModTranslations.FACTION_HAS_BEACON_ON_PLACE));
			return;
		}

		BlockPos pos = event.getPos();

        if (!player.serverLevel().canSeeSky(pos)) {
            event.setCanceled(true);
            player.sendSystemMessage(MessageUtil.Prefix.error(ModTranslations.BEACON_NOT_VISIBLE));
            return;
        }

		faction.setBeaconPos(pos);

		for (UUID memberUUID : faction.getMembers().keySet()) {
			ServerPlayer member = player.getServer().getPlayerList().getPlayer(memberUUID);
			if (member != null) {
				member.sendSystemMessage(MessageUtil.Prefix.success(ModTranslations.PLACE_SUCCESS));
			}
		}
		data.save(player.serverLevel());
	}


	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		if (!(event.getPlayer() instanceof ServerPlayer player)) return;
		if (event.getState().getBlock() != Blocks.BEACON) return;

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		String beaconFactionName = data.getFactionByBeaconPosition(event.getPos());
		if (beaconFactionName == null) return;

		Faction beaconFaction = data.getFaction(beaconFactionName);
		if (beaconFaction == null) return;

		Faction breakerFaction = data.getFactionFromPlayer(player.getUUID());

		if (breakerFaction == null) {
			event.setCanceled(true);
			player.sendSystemMessage(MessageUtil.Prefix.error(ModTranslations.PLAYER_NOT_IN_FACTION_ON_BREAK));
			return;
		}

		if (breakerFaction.getName().equals(beaconFactionName)) {
			event.setCanceled(true);
			player.sendSystemMessage(MessageUtil.Prefix.error(ModTranslations.SAME_FACTION_ON_BREAK));
			return;
		}

		if (ModConfigs.commonConfig.lastManOnline.get()) {
			List<ServerPlayer> onlinePlayers = new ArrayList<>();
			for (UUID memberUUID : beaconFaction.getMembers().keySet()) {
				ServerPlayer member = Objects.requireNonNull(player.getServer()).getPlayerList().getPlayer(memberUUID);
				if (member != null) {
					onlinePlayers.add(member);
				}
			}
			if (onlinePlayers.isEmpty()) {
				event.setCanceled(true);
				player.sendSystemMessage(MessageUtil.Prefix.error(ModTranslations.NO_FACTION_MEMBERS_ONLINE_ON_BREAK));
				return;
			}
		}

		event.setCanceled(true);
		ServerLevel level = (ServerLevel) event.getLevel();
		level.setBlock(beaconFaction.getBeaconPos(), Blocks.AIR.defaultBlockState(), 3);
		beaconFaction.removeBeacon();

		for (UUID memberUUID : beaconFaction.getMembers().keySet()) {
			ServerPlayer member = player.getServer().getPlayerList().getPlayer(memberUUID);
			if (member != null) {
				member.sendSystemMessage(MessageUtil.Prefix.error(ModTranslations.BREAK_SUCCESS_MEMBER));
			}
		}

		for (ServerPlayer onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
			if (!onlinePlayer.getUUID().equals(player.getUUID())) {
				onlinePlayer.sendSystemMessage(MessageUtil.Prefix.formattedMessage(
						ModTranslations.BREAK_SUCCESS_BROADCAST,
						List.of(ChatFormatting.AQUA, ChatFormatting.BOLD), beaconFactionName));
			}
		}

		data.hardcoreFaction(beaconFactionName, level);
		player.sendSystemMessage(MessageUtil.Prefix.success(ModTranslations.BREAK_SUCCESS, beaconFactionName));
	}

    public static boolean searchForBlockInSphere(Block block, Level level, BlockPos center, int radius, int blocksAmount) {
        BlockPos min = center.offset(-radius, -radius, -radius);
        BlockPos max = center.offset(radius, radius, radius);
        int radiusSq =  radius * radius;

        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (pos.distSqr(center) <= radiusSq && level.getBlockState(pos).is(block)) {
                count++;
                if (count >= blocksAmount) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean searchForBlockInSphere(TagKey<Block> block, Level level, BlockPos center, int radius, int blocksAmount) {
        BlockPos min = center.offset(-radius, -radius, -radius);
        BlockPos max = center.offset(radius, radius, radius);
        int radiusSq =  radius * radius;

        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (pos.distSqr(center) <= radiusSq && level.getBlockState(pos).is(block)) {
                count++;
                if (count >= blocksAmount) {
                    return true;
                }
            }
        }
        return false;
    }

}
