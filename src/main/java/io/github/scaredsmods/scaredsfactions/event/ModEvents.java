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
package io.github.scaredsmods.scaredsfactions.event;

import com.mojang.authlib.GameProfile;
import io.github.scaredsmods.scaredsfactions.ModConfigs;
import io.github.scaredsmods.scaredsfactions.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.command.FactionCommand;
import io.github.scaredsmods.scaredsfactions.faction.Faction;
import io.github.scaredsmods.scaredsfactions.util.MessageUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ScaredsFactionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		FactionCommand.register(event.getDispatcher());
	}


	@SubscribeEvent
	public static void onBeaconInteract(PlayerInteractEvent.RightClickBlock event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (event.getLevel().getBlockState(event.getPos()).getBlock() != Blocks.BEACON) return;

		Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());
		String factionName = data.getFactionByBeaconPosition(event.getPos());
		if (factionName == null) return;
		event.setCanceled(true);
	}


	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (event.isEndConquered()) return;
		Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());
		if (faction == null) return;
		if (!data.isHardcored(faction.getName())) {

			return;
		}
		data.eliminatePlayer(player.getUUID());
		data.setDirty();
		player.setGameMode(GameType.SPECTATOR);
		player.sendSystemMessage(MessageUtil.Prefix.error("Your faction's beacon was destroyed before you died. Nothing is anchoring you to life anymore."));
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		GameProfile profile = player.getGameProfile();
		ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
		CompoundTag nbt = new CompoundTag();
		nbt.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), profile));
		stack.setTag(nbt);
		player.addItem(stack);
	}

	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());
		if (faction == null) return;

		if (!data.isHardcored(faction.getName())) return;
		if (data.isEliminated(player.getUUID())) {
			player.setGameMode(GameType.SPECTATOR);
			player.sendSystemMessage(MessageUtil.Prefix.info("You were previously eliminated! Spectate your team!"));
			return;
		}

		if (data.hasBeacon(faction.getName())) {
			player.getServer().execute(() -> {
				if (ModConfigs.commonConfig.respawnPlayerAtFactionBeacon.get()) {
					player.setRespawnPosition(Level.OVERWORLD, data.getBeaconPosition(faction.getName()).above(), 0.0F, true, false);
					player.sendSystemMessage(MessageUtil.Prefix.info("Your faction have placed their beacon. Your respawn position has been set to that beacon!"));
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

		ItemStack item = player.getMainHandItem();
		CompoundTag tag = item.getTag();
		if (tag == null || !tag.getBoolean("respawn_beacon")) return;

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

		Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());

		if (faction == null) return;
		if (!faction.getOwner().equals(player.getUUID())) return;
		if (data.hasBeacon(faction.getName())) return;

		BlockPos pos = event.getPos();
		data.setBeaconPosition(faction.getName(), pos);

		for (UUID memberUUID : faction.getMembers().keySet()) {
			ServerPlayer member = player.getServer().getPlayerList().getPlayer(memberUUID);
			if (member != null) {
				member.sendSystemMessage(MessageUtil.Prefix.success("Your faction's respawn beacon has been placed!"));
			}
		}
		data.setDirty();
	}


	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		if (!(event.getPlayer() instanceof ServerPlayer player)) return;
		if (event.getState().getBlock() != Blocks.BEACON) return;

		Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());
		String beaconFactionName = data.getFactionByBeaconPosition(event.getPos());

		if (beaconFactionName == null) return;

		Faction beaconFaction = data.getFaction(beaconFactionName);
		Faction breakerFaction = data.getFactionFromPlayer(player.getUUID());

		if (breakerFaction == null) {
			event.setCanceled(true);
			player.sendSystemMessage(MessageUtil.Prefix.error("You aren't in a faction! You are considered neutral and cannot break a faction's beacon!"));
			return;
		}

		if (breakerFaction.getName().equals(beaconFactionName)) {
			event.setCanceled(true);
			player.sendSystemMessage(MessageUtil.Prefix.error("You cannot move your own faction's beacon!"));
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
		level.setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);

		data.removeBeacon(beaconFactionName);
		data.hardcoreFaction(beaconFactionName);

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
		player.sendSystemMessage(MessageUtil.Prefix.success(String.format("You just destroyed %s's beacon. Kill them to knock them out!", beaconFactionName)));
		data.setDirty();
	}

}
