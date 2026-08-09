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
import io.github.scaredsmods.scaredsfactions.common.component.ModDataComponents;
import io.github.scaredsmods.scaredsfactions.common.component.RespawnBeaconDataComponent;
import io.github.scaredsmods.scaredsfactions.common.config.ModConfigs;
import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.common.command.FactionCommand;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData;
import io.github.scaredsmods.scaredsfactions.common.network.packet.SyncFactionDataS2CPacket;
import io.github.scaredsmods.scaredsfactions.common.util.MessageUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@EventBusSubscriber(modid = ScaredsFactionMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ModGameEvents {

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		FactionCommand.register(event.getDispatcher());
	}

	@SubscribeEvent
	public static void preventVanillaPvp(LivingIncomingDamageEvent event) {
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

		Boolean factionSetting = victimFaction.getSettingValue("enableVanillaFriendlyFire", BooleanFactionSetting.class);
		boolean isVanillaPvpEnabled = (factionSetting != null && factionSetting);
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
		player.sendSystemMessage(MessageUtil.Prefix.error("Your faction's beacon was destroyed before you died. Nothing is anchoring you to life anymore."));
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		GameProfile profile = player.getGameProfile();
		ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
		stack.set(DataComponents.PROFILE, new ResolvableProfile(profile));
		player.addItem(stack);
	}

	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
        PacketDistributor.sendToPlayer(player, new SyncFactionDataS2CPacket(data.getFactions()));
		Faction faction = data.getFactionFromPlayer(player.getUUID());

		if (faction == null) return;

		if (!data.isHardcored(faction.getName())) return;
		if (faction.isEliminated(player.getUUID())) {
			player.setGameMode(GameType.SPECTATOR);
			player.sendSystemMessage(MessageUtil.Prefix.info("You were previously eliminated! Spectate your team!"));
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
			player.sendSystemMessage(MessageUtil.Prefix.error("You must hold the beacon in your main hand to place it!"));
			return;
		}

		ItemStack mainHandStack = player.getMainHandItem();

        RespawnBeaconDataComponent respawnBeaconDataComponent = mainHandStack.get(ModDataComponents.RESPAWN_BEACON.get());
        if (respawnBeaconDataComponent != null && !respawnBeaconDataComponent.isRespawnBeacon()) return;

		if (!(player.serverLevel().dimension().equals(Level.OVERWORLD))) {
			event.setCanceled(true);
			player.sendSystemMessage(MessageUtil.Prefix.error("You can only place your beacon in the overworld!"));
			return;
		}

		if (player.gameMode.isCreative()) {
			event.setCanceled(true);
			player.sendSystemMessage(MessageUtil.Prefix.error("You must be in survival to place your beacon!"));
			return;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());

		if (faction == null) return;
		if (!faction.getOwner().equals(player.getUUID())) {
			event.setCanceled(true);
			player.sendSystemMessage(MessageUtil.Prefix.error("Only the faction leader can do this!"));
			return;
		}
		if (faction.hasBeacon()) {
			event.setCanceled(true);
			player.sendSystemMessage(MessageUtil.Prefix.error("Your faction already has a beacon!"));
			return;
		}

		BlockPos pos = event.getPos();
		faction.setBeaconPos(pos);

		for (UUID memberUUID : faction.getMembers().keySet()) {
			ServerPlayer member = player.getServer().getPlayerList().getPlayer(memberUUID);
			if (member != null) {
				member.sendSystemMessage(MessageUtil.Prefix.success("Your faction's respawn beacon has been placed!"));
			}
		}
		data.save(player.serverLevel());
        player.getServer().saveAllChunks(false, true, false);
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
			player.sendSystemMessage(MessageUtil.Prefix.error("You aren't in a faction! You are considered neutral and cannot break a faction's beacon!"));
			return;
		}

		if (breakerFaction.getName().equals(beaconFactionName)) {
			event.setCanceled(true);
			player.sendSystemMessage(MessageUtil.Prefix.error("Use /faction manage to move your beacon!"));
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
				player.sendSystemMessage(MessageUtil.Prefix.error("There must be at least one player of this faction online to break this faction's beacon. Currently, no one of this faction is online!"));
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
				member.sendSystemMessage(MessageUtil.Prefix.error("Your faction's respawn beacon was destroyed! You are on your last life!"));
			}
		}

		for (ServerPlayer onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
			if (!onlinePlayer.getUUID().equals(player.getUUID())) {
				onlinePlayer.sendSystemMessage(MessageUtil.Prefix.formattedMessage(
						String.format("%s's beacon has been broken! Finish them!", beaconFactionName),
						ChatFormatting.AQUA, ChatFormatting.BOLD));
			}
		}

		data.hardcoreFaction(beaconFactionName, level);
        player.getServer().saveAllChunks(false, true, false);
		player.sendSystemMessage(MessageUtil.Prefix.success(String.format("You just destroyed %s's beacon. Kill them to knock them out!", beaconFactionName)));
	}

}
