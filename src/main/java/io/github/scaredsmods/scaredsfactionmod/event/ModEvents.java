/*
*  Copyright (C) 2025 ScaredRabbitNL
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
package io.github.scaredsmods.scaredsfactionmod.event;


import com.mojang.authlib.GameProfile;
import io.github.scaredsmods.scaredsfactionmod.ModConfigs;
import io.github.scaredsmods.scaredsfactionmod.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactionmod.command.FactionCommand;
import io.github.scaredsmods.scaredsfactionmod.faction.Faction;
import io.github.scaredsmods.scaredsfactionmod.faction.InviteManager;
import io.github.scaredsmods.scaredsfactionmod.faction.PersistentData;
import io.github.scaredsmods.scaredsfactionmod.util.PrefixUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = ScaredsFactionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

	@SubscribeEvent
	public static void onCommandsRegister(RegisterCommandsEvent event) {
		FactionCommand.register(event.getDispatcher());
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		InviteManager.removeInvite(event.getEntity().getUUID());
	}

	@SubscribeEvent
	public static void disableInFactionVanillaFriendlyFire(LivingHurtEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer victim)) return;
		if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;

		PersistentData data = PersistentData.get(victim.serverLevel());
		Faction victimFaction = data.getFactionByPlayer(victim.getUUID());
		Faction attackerFaction = data.getFactionByPlayer(attacker.getUUID());

		if (victimFaction == null || attackerFaction == null) return;
		if (!victimFaction.getName().equals(attackerFaction.getName())) return;

		if (!(ModConfigs.commonConfig.enableVanillaFriendlyFire.get())) {
			event.setCanceled(true);
		}

		attacker.sendSystemMessage(PrefixUtil.error("You cannot attack your own faction members!"));
	}


	@SubscribeEvent
	public static void onBeaconInteract(PlayerInteractEvent.RightClickBlock event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (event.getLevel().getBlockState(event.getPos()).getBlock() != Blocks.BEACON) return;

		PersistentData data = PersistentData.get(player.serverLevel());
		if (data.getBeaconFaction(event.getPos()) == null) return;

		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (event.getPlacedBlock().getBlock() != Blocks.BEACON) return;
		if (!(player.serverLevel().dimension().equals(Level.OVERWORLD))) {
			event.setCanceled(true);
			player.sendSystemMessage(PrefixUtil.error("You can only place your beacon in the overworld!"));
			return;
		}

		PersistentData data = PersistentData.get(player.serverLevel());
		Faction faction = data.getFactionByPlayer(player.getUUID());

		if (faction == null) return;
		if (!faction.getOwner().equals(player.getUUID())) return;
		if (data.hasBeacon(faction.getName())) return;

		BlockPos pos = event.getPos();
		data.setBeacon(faction.getName(), pos);


		if (ModConfigs.commonConfig.respawnPlayerAtFactionBeacon.get() == true){
			player.setRespawnPosition(Level.OVERWORLD, pos.above(), 0.0F, true, false);
		}

		for (UUID memberUUID : faction.getMembers()) {
			ServerPlayer member = player.getServer().getPlayerList().getPlayer(memberUUID);
			if (member != null) {
				if (ModConfigs.commonConfig.respawnPlayerAtFactionBeacon.get() == true) {
					member.setRespawnPosition(Level.OVERWORLD, pos.above(), 0.0F, true, false);
				}
				member.sendSystemMessage(PrefixUtil.success("Your faction's respawn beacon has been placed!"));
			}
		}
	}

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		if (!(event.getPlayer() instanceof ServerPlayer player)) return;
		if (event.getState().getBlock() != Blocks.BEACON) return;

		PersistentData data = PersistentData.get(player.serverLevel());
		String beaconFactionName = data.getBeaconFaction(event.getPos());

		if (beaconFactionName == null) return;

		Faction beaconFaction = data.getFaction(beaconFactionName);
		Faction breakerFaction = data.getFactionByPlayer(player.getUUID());

		if (breakerFaction == null) {
			event.setCanceled(true);
			player.sendSystemMessage(PrefixUtil.error("You aren't in a faction! You are considered neutral and cannot break a faction's beacon!"));
			return;
		}

		if (breakerFaction.getName().equals(beaconFactionName)) {
			event.setCanceled(true);
			player.sendSystemMessage(PrefixUtil.error("You cannot move your own faction's beacon!"));
			return;
		}

		ServerLevel level = (ServerLevel) event.getLevel();
		level.setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);

		data.removeBeacon(beaconFactionName);
		data.addHardcored(beaconFactionName);

		if (ModConfigs.commonConfig.respawnPlayerAtFactionBeacon.get() == true) {
			player.setRespawnPosition(Level.OVERWORLD, null, 0.0F, false, false);
		}
		for (UUID memberUUID : beaconFaction.getMembers()) {
			ServerPlayer member = player.getServer().getPlayerList().getPlayer(memberUUID);
			if (member != null) {
				if (ModConfigs.commonConfig.respawnPlayerAtFactionBeacon.get() == true) {
					member.setRespawnPosition(Level.OVERWORLD, null, 0.0F, false, false);
				}
				member.sendSystemMessage(PrefixUtil.error("Your faction's respawn beacon was destroyed! You are on your last life!"));
			}
		}

		for (ServerPlayer onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
			onlinePlayer.sendSystemMessage(PrefixUtil.formattedMessage(
					String.format("%s's beacon has been broken! Finish them!", beaconFactionName),
					ChatFormatting.AQUA, ChatFormatting.BOLD));
		}

		player.sendSystemMessage(PrefixUtil.success(String.format("You just destroyed %s's beacon. Kill them to knock them out!", beaconFactionName)));
	}


	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;

		PersistentData data = PersistentData.get(player.serverLevel());
		Faction faction = data.getFactionByPlayer(player.getUUID());

		if (faction == null) return;
		if (!data.isHardcored(faction.getName())) return;

		data.eliminatePlayer(player.getUUID());
		player.setGameMode(GameType.SPECTATOR);
		player.sendSystemMessage(PrefixUtil.error("Your faction's beacon was destroyed. You have no lifeline. Go on to spectate your team!"));
	}


	@SubscribeEvent
	public static void dropPlayerHeadOnDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		GameProfile profile = player.getGameProfile();
		CompoundTag nbt = new CompoundTag();
		nbt.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), profile));
		ItemStack stack = new ItemStack(Items.PLAYER_HEAD, 1);
		stack.setTag(nbt);
		player.drop(stack, false);
	}

	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;

		PersistentData data = PersistentData.get(player.serverLevel());
		Faction faction = data.getFactionByPlayer(player.getUUID());

		if (faction == null) return;

		if (data.isEliminated(player.getUUID())) {
			player.setGameMode(GameType.SPECTATOR);
			player.sendSystemMessage(PrefixUtil.error("Your faction's beacon was destroyed. You have no lifeline. Go on to spectate your team!"));
		}
	}
}
