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
package io.github.scaredsmods.scaredsfactions.common.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.BooleanFactionSetting;
import io.github.scaredsmods.scaredsfactions.common.ModConfigs;
import io.github.scaredsmods.scaredsfactions.common.ModPermissions;
import io.github.scaredsmods.scaredsfactions.client.screen.menu.ConfirmTransferOwnershipMenu;
import io.github.scaredsmods.scaredsfactions.client.screen.menu.ManageFactionMenu;
import io.github.scaredsmods.scaredsfactions.common.command.argument.ArrayEnumArgument;
import io.github.scaredsmods.scaredsfactions.common.compat.luckperms.LuckPermsAPICompat;
import io.github.scaredsmods.scaredsfactions.common.config.LanguageOptions;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSettings;
import io.github.scaredsmods.scaredsfactions.common.faction.InviteManager;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.AbstractFactionSetting;
import io.github.scaredsmods.scaredsfactions.server.network.packet.ModScreens;
import io.github.scaredsmods.scaredsfactions.common.util.MessageUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.*;
import java.util.stream.Collectors;

public class FactionCommand {

	public static final SuggestionProvider<CommandSourceStack> SUGGEST_PLAYERS_WITHOUT_FACTION = (ctx, builder) -> {
		ServerPlayer player = ctx.getSource().getPlayer();
		ServerLevel level = player.serverLevel();
		FactionSavedData data = FactionSavedData.getSavedData(level);
		level.getServer().getPlayerList().getPlayers().stream()
				.filter(p -> data.getFactionFromPlayer(p.getUUID()) == null)
				.forEach(p -> builder.suggest(p.getDisplayName().getString()));
		return builder.buildFuture();
	};

	public static final SuggestionProvider<CommandSourceStack> SUGGEST_PLAYERS_WITHIN_FACTION = (ctx, builder) -> {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) return builder.buildFuture();
		ServerLevel level = player.serverLevel();
		FactionSavedData data = FactionSavedData.getSavedData(level);
		Faction faction = data.getFactionFromPlayer(player.getUUID());
		if (faction != null) {
			faction.getMembers().keySet().forEach(uuid -> {
				ServerPlayer member = level.getServer().getPlayerList().getPlayer(uuid);
				if (member != null) {
					builder.suggest(member.getDisplayName().getString());
				}
			});
		}
		return builder.buildFuture();
	};

	public static final SuggestionProvider<CommandSourceStack> SUGGEST_FACTION_NAMES = (ctx, builder) -> {
		FactionSavedData data = FactionSavedData.getSavedData(ctx.getSource().getLevel());
		data.getFactions().keySet().forEach(name -> {
			String displayName = name.replaceAll("§[0-9a-fk-or]", "");
			builder.suggest(displayName);
		});
		return builder.buildFuture();
	};
	public static final SuggestionProvider<CommandSourceStack> SUGGEST_ALLIES = (ctx, builder) -> {
		FactionSavedData data = FactionSavedData.getSavedData(ctx.getSource().getLevel());
		data.getAlliedFactions().keySet().forEach(builder::suggest);
		return builder.buildFuture();
	};

	public static final SuggestionProvider<CommandSourceStack> SUGGEST_INVITED_FACTIONS = (ctx, builder) -> {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player != null)  {
			String name = InviteManager.getPendingInvite(player.getUUID());
			String displayName = name.replaceAll("§[0-9a-fk-or]", "");
			builder.suggest(displayName);
		}
		return builder.buildFuture();
	};

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("faction").executes(FactionCommand::help)
				.requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_USE_ROOT) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL))
				/* TODO: Rework alliance system
				.then(Commands.literal("ally")
						.then(Commands.argument("name", StringArgumentType.word()).suggests(SUGGEST_ALLIES).executes(ctx -> addAlly(ctx, StringArgumentType.getString(ctx, "name"))))
						.then(Commands.argument("name", StringArgumentType.string()).suggests(SUGGEST_ALLIES).executes(ctx -> addAlly(ctx, StringArgumentType.getString(ctx, "name"))))
						.then(Commands.argument("name", StringArgumentType.greedyString()).suggests(SUGGEST_ALLIES).executes(ctx -> addAlly(ctx, StringArgumentType.getString(ctx, "name"))))
				*/
				.then(Commands.literal("create")
						.requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_CREATE_FACTION) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL))
						.then(Commands.argument("name", StringArgumentType.greedyString())
								.executes(ctx -> createFaction(ctx, StringArgumentType.getString(ctx, "name")))))
				.then(Commands.literal("debug")
						.requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_DEBUG) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL) || source.hasPermission(4))
						.then(Commands.literal("open_screen")
								.then(Commands.argument("screen", ArrayEnumArgument.enumArgument(ModScreens.class, ModScreens.getEntries().stream()
												.filter(screen -> screen != ModScreens.CONFIRM_TRANSFER && screen != ModScreens.CLOSE)
												.toArray(ModScreens[]::new)))
										.executes(ctx -> openScreenDebugCommand(ctx, ctx.getArgument("screen", ModScreens.class))))
								.then(Commands.literal("CONFIRM_TRANSFER")
										.then(Commands.argument("targetUUID", StringArgumentType.greedyString())
												.executes(ctx -> openConfirmTransferScreen(ctx, StringArgumentType.getString(ctx, "targetUUID")))))))
				.then(Commands.literal("demote")
						.requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_DEMOTE_PLAYER) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL))
						.then(Commands.argument("target", EntityArgument.player())
								.suggests(SUGGEST_PLAYERS_WITHIN_FACTION)
								.executes(ctx -> demotePlayer(ctx, EntityArgument.getPlayer(ctx, "target")))))
				.then(Commands.literal("disband")
						.requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_DISBAND_FACTION) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL))
						.executes(FactionCommand::disbandFaction)
						.then(Commands.argument("name", StringArgumentType.greedyString())
							.requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_USE_DISBAND_BY_NAME) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL))
							.suggests(SUGGEST_FACTION_NAMES)
							.executes(ctx -> disbandFaction(ctx, StringArgumentType.getString(ctx, "name")))))
				.then(Commands.literal("help").requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_USE_HELP) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL)).executes(FactionCommand::help))
				.then(Commands.literal("home").requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_TELEPORT_TO_BEACON) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL)).executes(FactionCommand::teleportToBeacon))
				.then(Commands.literal("info")
						.requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_GET_FACTION_INFO) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL))
						.then(Commands.argument("factionName", StringArgumentType.greedyString())
								.suggests(SUGGEST_FACTION_NAMES)
								.executes(ctx -> factionInfo(ctx, StringArgumentType.getString(ctx, "factionName")))))
				.then(Commands.literal("invite")
						.requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_INVITE_PLAYER_TO_FACTION) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL))
						.then(Commands.argument("target", EntityArgument.player()).suggests(SUGGEST_PLAYERS_WITHOUT_FACTION).executes(ctx -> invitePlayer(ctx, EntityArgument.getPlayer(ctx, "target"))))
						.then(Commands.literal("accept")
								.then(Commands.argument("name", StringArgumentType.greedyString()).suggests(SUGGEST_INVITED_FACTIONS).executes(ctx -> acceptInvite(ctx, StringArgumentType.getString(ctx, "name"))))))
				.then(Commands.literal("kick")
						.requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_KICK_PLAYER_FROM_FACTION) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL))
						.then(Commands.argument("target", EntityArgument.player())
								.suggests(SUGGEST_PLAYERS_WITHIN_FACTION).executes(ctx -> kickPlayer(ctx, EntityArgument.getPlayer(ctx, "target")))))
				.then(Commands.literal("leave").requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_LEAVE_FACTION) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL)).executes(FactionCommand::leaveFaction))
				.then(Commands.literal("list").requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_LIST_FACTIONS) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL)).executes(FactionCommand::listFactions))
				.then(Commands.literal("manage").requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_MANAGE_FACTION) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL)).executes(FactionCommand::manage))
				.then(Commands.literal("promote")
						.requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_PROMOTE_PLAYER) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL))
						.then(Commands.argument("target", EntityArgument.player())
								.suggests(SUGGEST_PLAYERS_WITHIN_FACTION)
								.executes(ctx -> promotePlayer(ctx, EntityArgument.getPlayer(ctx, "target")))))
		);
	}

	private static int openConfirmTransferScreen(CommandContext<CommandSourceStack> ctx, String targetUUID) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You must be a player to execute this command!"));
			return 0;
		}

		NetworkHooks.openScreen(player, new SimpleMenuProvider(
						(pContainerId, pPlayerInventory, pPlayer) -> new ConfirmTransferOwnershipMenu(pContainerId, pPlayerInventory, UUID.fromString(targetUUID)),
						Component.literal("Confirm Transfer?")),
				buf -> buf.writeUUID(UUID.fromString(targetUUID)));
		return 1;
	}

	private static int openScreenDebugCommand(CommandContext<CommandSourceStack> ctx, ModScreens screen) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You must be a player to execute this command!"));
			return 0;
		}

		if (screen == ModScreens.CLOSE) {
			player.closeContainer();
			return 1;
		}

		openScreen(player, screen);
		return 1;
	}

	private static int manage(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You must be a player to execute this command!"));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());
		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You must be in a faction to execute this command!"));
			return 0;
		}

		Faction.Rank playerRank = faction.getMembers().get(player.getUUID());
		if (playerRank != Faction.Rank.GENERALISSIMUS && playerRank != Faction.Rank.STADHOUDER) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You don't have the required rank to do this!"));
			return 0;
		}

		NetworkHooks.openScreen(player, new SimpleMenuProvider(
				(pContainerId, pPlayerInventory, pPlayer) -> new ManageFactionMenu(pContainerId, pPlayerInventory),
				Component.literal(faction.getName().replace("&", "§"))));
		return 1;
	}

	private static int kickPlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You must be a player to execute this command!"));
			return 0;
		}
		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());
		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You must be in a faction to execute this command!"));
			return 0;
		}

		Faction.Rank playerRank = faction.getMembers().get(player.getUUID());
		if ((playerRank != Faction.Rank.GENERALISSIMUS && playerRank != Faction.Rank.STADHOUDER)) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("Only the leader of the faction can kick players from the faction!"));
			return 0;
		}

		if (target.getUUID().equals(player.getUUID())) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You cannot kick yourself!"));
			return 0;
		}

		if (!faction.getMembers().containsKey(target.getUUID())) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("That player is not in your faction!"));
			return 0;
		}

		faction.getMembers().remove(target.getUUID());
		data.markDirty(player.serverLevel());
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(String.format("Successfully kicked %s from the faction!", target.getDisplayName().getString())), false);
		return 1;
	}

	private static int promotePlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());

		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You are not in a faction!"));
			return 0;
		}
		if (!faction.getMembers().containsKey(target.getUUID())) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("Can't promote a player that isn't under your command!"));
			return 0;
		}

		Faction.Rank playerRank = faction.getMembers().get(player.getUUID());
		Faction.Rank targetRank = faction.getMembers().get(target.getUUID());

		if (!Arrays.asList(playerRank.getManageableRanks()).contains(targetRank)) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You cannot manage players of equal or higher rank!"));
			return 0;
		}

		int newRankId = targetRank.getId() + 1;
		if (!Arrays.asList(playerRank.getManageableRanks()).contains(Faction.Rank.getRankById(newRankId))) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You cannot manage players of equal or higher rank!"));
			return 0;
		}

		faction.getMembers().put(target.getUUID(), Faction.Rank.getRankById(newRankId));
		data.markDirty(player.serverLevel());
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(String.format("You successfully promoted §7%s §ato %s!", target.getDisplayName(), Faction.Rank.getRankById(newRankId).getName())) , false);
		return 1;
	}

	private static int listFactions(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
			return 0;
		}
		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		if (data.getFactions().isEmpty()) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("No factions exist yet!"));
			return 1;
		}

		Collection<Faction> factions = data.getFactions().values();
		List<Faction> visibleFactions = factions.stream()
				.filter(faction -> faction.getSettingValue(FactionSettings.INFO_VISIBLE.getNbtId(), BooleanFactionSetting.class))
				.toList();

		if (visibleFactions.isEmpty()) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("There are no visible factions!"));
			return 0;
		}

		Component divider = Component.literal("====== ")
				.withStyle(ChatFormatting.DARK_GRAY)
				.append(MessageUtil.Prefix.PREFIX_PLAIN)
				.append(Component.literal(" ======").withStyle(ChatFormatting.DARK_GRAY));

		String factionList = visibleFactions.stream()
				.map(faction -> faction.getName().replace("&", "§") + "§r (" + faction.getMembers().size() + " members)")
				.collect(Collectors.joining(", "));

		if (factionList.isEmpty()) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("There are no visible factions!"));
			return 0;
		}

		ctx.getSource().sendSuccess(() -> divider, false);
		ctx.getSource().sendSuccess(() -> Component.literal(factionList).withStyle(ChatFormatting.GRAY), false);
		ctx.getSource().sendSuccess(() -> divider, false);
		return 1;
	}

	private static int leaveFaction(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());
		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be in a faction to use this command!"));
			return 0;
		}

		if (faction.getOwner().equals(player.getUUID())) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You are the owner of this faction! Disband the faction or transfer ownership first!"));
			return 0;
		}

		faction.getMembers().remove(player.getUUID());
		data.markDirty(player.serverLevel());
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success("You left " + faction.getName().replace("&", "§") + "§c!"), false);
		return 1;
	}

	private static int teleportToBeacon(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());
		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be in a faction to use this command!"));
			return 0;
		}

		BlockPos beaconPos = faction.getBeaconPos();

		if (!faction.hasBeacon()) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("Your faction doesn't have their beacon placed down yet!"));
			return 0;
		}

		long currentTime = System.currentTimeMillis() / 1000;
		if (ModConfigs.commonConfig.enableHomeCommandCooldown.get()) {
			long cooldownSeconds = ModConfigs.commonConfig.homeCommandCooldown.get();
			long lastUsed = faction.getHomeCooldown(player.getUUID());
			long remainingTime = (lastUsed + cooldownSeconds) - currentTime;
			if (remainingTime > 0) {
				long hours = remainingTime / 3600;
				long minutes = (remainingTime % 3600) / 60;
				long seconds = remainingTime % 60;
				ctx.getSource().sendFailure(MessageUtil.Prefix.error(String.format("You must wait another %s:%s:%s before using this command again!", hours, minutes, seconds)));
			}
			faction.setHomeCooldown(player.getUUID(), currentTime);
		}
		data.markDirty(player.serverLevel());
		player.teleportTo(beaconPos.getX() + 0.5, beaconPos.getY() + 1,  beaconPos.getZ() + 0.5);
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success("Successfully teleported to your faction's beacon!"), false);
		return 1;
	}

	private static int factionInfo(CommandContext<CommandSourceStack> ctx, String factionName) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
			return 0;
		}
		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFaction(factionName);
		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("That faction does not exist!"));
			return 0;
		}




		if (!faction.getSettingValue(FactionSettings.INFO_VISIBLE.getNbtId(), BooleanFactionSetting.class)) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("That faction wants to be private!"));
			return 0;
		}
		Component divider = Component.literal("====== ")
				.withStyle(ChatFormatting.DARK_GRAY)
				.append(MessageUtil.Prefix.PREFIX_PLAIN)
				.append(Component.literal(" ======").withStyle(ChatFormatting.DARK_GRAY));

		StringBuilder members = new StringBuilder();
		for (UUID memberUUID : faction.getMembers().keySet()) {
			Faction.Rank rank = faction.getMembers().get(memberUUID);
			String memberName = ctx.getSource().getServer().getProfileCache()
					.get(memberUUID)
					.map(GameProfile::getName)
					.orElse("Unknown Player");
			members.append(memberName).append(" (").append(rank.getName()).append("), ");
		}
		String membersStr = !members.isEmpty() ? members.substring(0, members.length() - 2) : "None";
		String alliesStr = faction.getAllies().isEmpty() ? "None" : String.join(", ", faction.getAllies());

		ctx.getSource().sendSuccess(() -> divider, false);
		ctx.getSource().sendSuccess(() -> Component.literal("Faction: " + faction.getName().replace("&" , "§")).withStyle(ChatFormatting.GRAY), false);
		ctx.getSource().sendSuccess(() -> Component.literal("Members: " + membersStr).withStyle(ChatFormatting.GRAY), false);
		//ctx.getSource().sendSuccess(() -> Component.literal("Allies: " + alliesStr).withStyle(ChatFormatting.GRAY), false);
		ctx.getSource().sendSuccess(() -> divider, false);
		return 1;
	}

	private static int invitePlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
			return 0;
		}
		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());
		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be in a faction to use this command!"));
			return 0;
		}
		Faction.Rank playerRank = faction.getMembers().get(player.getUUID());
		if (playerRank == Faction.Rank.PRIVATE) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You don't have permission to invite players!"));
			return 0;
		}
		if (data.getFactionFromPlayer(target.getUUID()) != null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("That player is already in a faction!"));
			return 0;
		}

		if (faction.getMembers().size() >= ModConfigs.commonConfig.maxMembers.get()) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("Your faction has reached the maximum number of members!"));
			return 0;
		}

		InviteManager.invite(target.getUUID(), faction.getName());
		data.markDirty(player.serverLevel());
		target.sendSystemMessage(MessageUtil.Prefix.success("You have been invited to join " + faction.getName().replace("&", "§") + "§a. Use /faction invite accept " + faction.getName().replace("&", "§") + "§a to accept!"), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.formattedMessage("Invited " + target.getName().getString() + " to your faction.", ChatFormatting.GOLD), false);
		return 1;
	}

	private static int disbandFaction(CommandContext<CommandSourceStack> ctx, String factionName) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFaction(factionName);

		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("This faction doesn't exist!"));
			return 0;
		}

		ServerLevel level = ctx.getSource().getLevel();
		BlockPos beaconPos = faction.getBeaconPos();
		if (beaconPos != null) {
			level.destroyBlock(beaconPos, false);
			faction.removeBeacon();
		}

		for (UUID memberUUID : faction.getMembers().keySet()) {
			ServerPlayer member = ctx.getSource().getServer().getPlayerList().getPlayer(memberUUID);
			if (member != null) {
				member.sendSystemMessage(MessageUtil.Prefix.error("Your faction has been disbanded by an admin!"));
				member.setRespawnPosition(Level.OVERWORLD, null, 0.0F, true, false);
				kickPlayer(ctx, member);
			}
		}

		data.removeFaction(faction, player.serverLevel());
		data.markDirty(player.serverLevel());
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success("Faction " + faction.getName().replace("&", "§") + "§r has been disbanded!"), false);
		return 1;
	}

	private static int disbandFaction(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());

		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You are not in a faction!"));
			return 0;
		}

		Faction.Rank playerRank = faction.getMembers().get(player.getUUID());
		if ( (playerRank != Faction.Rank.GENERALISSIMUS && playerRank != Faction.Rank.STADHOUDER) || faction.getOwner() == player.getUUID()) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("Only the faction leader can disband the faction!"));
			return 0;
		}

		ServerLevel level = ctx.getSource().getLevel();
		BlockPos beaconPos = faction.getBeaconPos();
		if (beaconPos != null) {
			level.destroyBlock(beaconPos, false);
			faction.removeBeacon();
		}

		for (UUID memberUUID : faction.getMembers().keySet()) {
			ServerPlayer member = ctx.getSource().getServer().getPlayerList().getPlayer(memberUUID);
			if (member != null) {
				if (!member.getUUID().equals(player.getUUID())) {
					member.sendSystemMessage(MessageUtil.Prefix.error("Your faction has been disbanded!"));
					kickPlayer(ctx, member);
				}
				member.setRespawnPosition(Level.OVERWORLD, null, 0.0F, true, false);
			}
		}

		data.removeFaction(faction, player.serverLevel());
		data.markDirty(player.serverLevel());
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success("You have disbanded your faction!"), false);
		return 1;
	}

	private static int demotePlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());

		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You are not in a faction!"));
			return 0;
		}
		if (!faction.getMembers().containsKey(target.getUUID())) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("That player is not in your faction!"));
			return 0;
		}

		Faction.Rank playerRank = faction.getMembers().get(player.getUUID());
		Faction.Rank targetRank = faction.getMembers().get(target.getUUID());

		if (!Arrays.asList(playerRank.getManageableRanks()).contains(targetRank)) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You cannot demote this player!"));
			return 0;
		}

		int newRankId = targetRank.getId() - 1;
		if (newRankId < 0) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("This player is already the lowest rank!"));
			return 0;
		}

		if (!Arrays.asList(playerRank.getManageableRanks()).contains(Faction.Rank.getRankById(newRankId))) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You cannot demote this player to that rank!"));
			return 0;
		}

		faction.getMembers().put(target.getUUID(), Faction.Rank.getRankById(newRankId));
		data.markDirty(player.serverLevel());
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(String.format("You successfully demoted §7%s §ato %s", target.getDisplayName(), Faction.Rank.getRankById(newRankId).getName())), false);
		return 1;
	}

	private static int createFaction(CommandContext<CommandSourceStack> ctx, String name) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
			return 0;
		}
		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());

		String formattedName = name.replace("&", "§");
		String strippedName = formattedName.replaceAll("§[0-9a-fk-or]", "");
		for (Faction faction : data.getFactions().values()) {
			String existingNamesStripped = faction.getName().replaceAll("§[0-9a-fk-or]", "");
			if (existingNamesStripped.equalsIgnoreCase(strippedName)) {
				ctx.getSource().sendFailure(MessageUtil.Prefix.error("This faction already exists! Please choose another name!"));
				return 0;
			}
		}

		List<AbstractFactionSetting<?, ?>> settings = Faction.createDefaultSettings();

		Faction faction = new Faction(name, player.getUUID(), settings);
		Faction.Rank rank = (ModConfigs.commonConfig.defaultOwnerRank.get() == LanguageOptions.PREFER_STADHOUDER) ? Faction.Rank.STADHOUDER : Faction.Rank.GENERALISSIMUS;
		faction.getMembers().put(player.getUUID(), rank);
		data.addFaction(faction, player.serverLevel());

		ItemStack beacon = new ItemStack(Items.BEACON);
		beacon.getOrCreateTag().putBoolean("respawn_beacon", true);
		beacon.setHoverName(Component.literal("Respawn Beacon")
				.withStyle(style -> style
						.withBold(true)
						.withColor(ChatFormatting.RED)
						.withItalic(false)));
		CompoundTag display = beacon.getOrCreateTagElement("display");
		ListTag lore = new ListTag();
		lore.add(StringTag.valueOf(Component.Serializer.toJson(
				Component.literal("This is your faction's respawn beacon!")
						.withStyle(style -> style
								.withColor(ChatFormatting.GRAY)
								.withItalic(false))
		)));
		lore.add(StringTag.valueOf(Component.Serializer.toJson(
				Component.literal("It functions as your bed, and lifeline!")
						.withStyle(style -> style
								.withColor(ChatFormatting.GRAY)
								.withItalic(false))
		)));

		lore.add(StringTag.valueOf(Component.Serializer.toJson(
				Component.literal("Hide it well: If other factions get a hold of it, you can no longer respawn!")
						.withStyle(style -> style
								.withBold(false)
								.withItalic(false)
								.withColor(ChatFormatting.GRAY))
		)));

		display.put("Lore", lore);
		player.getInventory().add(beacon);
		data.markDirty(player.serverLevel());
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success("Faction ")
				.copy()
				.append(Component.literal(formattedName))
				.append(Component.literal(" created!").withStyle(ChatFormatting.GREEN)), false);
		return 1;
	}

	private static int acceptInvite(CommandContext<CommandSourceStack> ctx, String name) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) return 0;
		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());

		if (data.getFactionFromPlayer(player.getUUID()) != null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You are already in a faction! Leave your current faction first before joining a new one!"));
			return 0;
		}

		if (!InviteManager.hasInvite(player.getUUID(), name)) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("You don't have any invites!"));
			return 0;
		}
		Faction faction = data.getFaction(name);
		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("This faction does not exist! Are you sure you spelled it correctly?"));
			return 0;
		}

		if (faction.getMembers().size() >= ModConfigs.commonConfig.maxMembers.get()) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error("Your faction has reached the maximum number of members!"));
			return 0;
		}

		faction.getMembers().put(player.getUUID(), Faction.Rank.PRIVATE);
		InviteManager.cancelInvite(player.getUUID());
		data.markDirty(player.serverLevel());

        Objects.requireNonNull(Objects.requireNonNull(ctx.getSource().getPlayer().getServer()).getPlayerList().getPlayer(faction.getOwner())).sendSystemMessage(MessageUtil.Prefix.info(String.format("§f%s §ahas joined your faction", player.getGameProfile().getName())));
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(String.format("You successfully joined %s!", name.replace("&", "§"))), false);
		return 1;
	}

	private static int addAlly(CommandContext<CommandSourceStack> ctx, String name) {
		return 1;
	}

	private static int help(CommandContext<CommandSourceStack> ctx) {
		Component divider = Component.literal("====== ")
				.withStyle(ChatFormatting.DARK_GRAY)
				.append(MessageUtil.Prefix.PREFIX_PLAIN)
				.append(Component.literal(" ======").withStyle(ChatFormatting.DARK_GRAY));

		ctx.getSource().sendSuccess(() -> divider, false);
		//ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction ally <name> - Formally create an alliance with another faction. (WIP)"), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction create <name> - Creates a faction and takes in a name as argument"), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction demote <player> - Demotes a player. (WIP)"), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction disband - Disbands your current faction if you are the leader (general)."), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction disband <name> - An admin command to remove any faction."), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction help - Shows this message"), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction home - Teleports the player to their respawn beacon."), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction info <name> - Displays information about a faction."), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction invite <player> - Invite a player that isn't in a faction to join your faction."), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction invite accept <name> - Accept an invite from a faction"), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction kick - Kicks a player from the faction. Only executable by the leader."), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction leave - Leave your current faction."), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction list - List all factions"), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction manage - Manage your faction if you are their leader."), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction promote <name> - Promotes a player to a new rank. (WIP)"), false);
		//ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction unally <name> - Terminate the formal alliance"), false);
		ctx.getSource().sendSuccess(() -> divider, false);
		return 1;
	}

	private static void openScreen(ServerPlayer player, ModScreens screen) {
		if (screen == ModScreens.CLOSE) {
			player.closeContainer();
			return;
		}
		NetworkHooks.openScreen(player, new SimpleMenuProvider(
				(id, inv, p) -> screen.createMenu(id, inv, player),
				screen.getTitle()
		), buf -> screen.writeBuf(player, buf));
	}
}
