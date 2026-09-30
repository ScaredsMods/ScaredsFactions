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
import io.github.scaredsmods.scaredsfactions.common.ModTranslations;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.AbstractFactionSetting;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.BooleanFactionSetting;
import io.github.scaredsmods.scaredsfactions.client.screen.menu.ConfirmTransferOwnershipMenu;
import io.github.scaredsmods.scaredsfactions.client.screen.menu.ManageFactionMenu;
import io.github.scaredsmods.scaredsfactions.common.ModConfigs;
import io.github.scaredsmods.scaredsfactions.common.command.argument.ArrayEnumArgument;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSettings;
import io.github.scaredsmods.scaredsfactions.common.faction.InviteManager;
import io.github.scaredsmods.scaredsfactions.common.util.MessageUtil;
import io.github.scaredsmods.scaredsfactions.server.network.packet.ModScreens;
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
				/* TODO: Rework alliance system
				.then(Commands.literal("ally")
						.then(Commands.argument("name", StringArgumentType.word()).suggests(SUGGEST_ALLIES).executes(ctx -> addAlly(ctx, StringArgumentType.getString(ctx, "name"))))
						.then(Commands.argument("name", StringArgumentType.string()).suggests(SUGGEST_ALLIES).executes(ctx -> addAlly(ctx, StringArgumentType.getString(ctx, "name"))))
						.then(Commands.argument("name", StringArgumentType.greedyString()).suggests(SUGGEST_ALLIES).executes(ctx -> addAlly(ctx, StringArgumentType.getString(ctx, "name"))))
				*/
				.then(Commands.literal("create")
						.then(Commands.argument("name", StringArgumentType.greedyString())
								.executes(ctx -> createFaction(ctx, StringArgumentType.getString(ctx, "name")))))
				.then(Commands.literal("debug").requires(source -> source.hasPermission(4))
						.then(Commands.literal("list_beacons").executes(FactionCommand::listBeacons))
						.then(Commands.literal("open_screen")
								.then(Commands.argument("screen", ArrayEnumArgument.enumArgument(ModScreens.class, ModScreens.getEntries().stream()
												.filter(screen -> screen != ModScreens.CONFIRM_TRANSFER && screen != ModScreens.CLOSE)
												.toArray(ModScreens[]::new)))
										.executes(ctx -> openScreenDebugCommand(ctx, ctx.getArgument("screen", ModScreens.class))))
								.then(Commands.literal("CONFIRM_TRANSFER")
										.then(Commands.argument("targetUUID", StringArgumentType.greedyString())
												.executes(ctx -> openConfirmTransferScreen(ctx, StringArgumentType.getString(ctx, "targetUUID")))))))

				.then(Commands.literal("demote")
						.then(Commands.argument("target", EntityArgument.player())
								.suggests(SUGGEST_PLAYERS_WITHIN_FACTION)
								.executes(ctx -> demotePlayer(ctx, EntityArgument.getPlayer(ctx, "target")))))
				.then(Commands.literal("disband")
						.executes(FactionCommand::disbandFaction)
						.then(Commands.argument("name", StringArgumentType.greedyString())
							.suggests(SUGGEST_FACTION_NAMES)
							.executes(ctx -> disbandFaction(ctx, StringArgumentType.getString(ctx, "name")))))
				.then(Commands.literal("help").executes(FactionCommand::help))
				.then(Commands.literal("home").executes(FactionCommand::teleportToBeacon))
				.then(Commands.literal("info")
						.then(Commands.argument("factionName", StringArgumentType.greedyString())
								.suggests(SUGGEST_FACTION_NAMES)
								.executes(ctx -> factionInfo(ctx, StringArgumentType.getString(ctx, "factionName")))))
				.then(Commands.literal("invite")
						.then(Commands.argument("target", EntityArgument.player()).suggests(SUGGEST_PLAYERS_WITHOUT_FACTION).executes(ctx -> invitePlayer(ctx, EntityArgument.getPlayer(ctx, "target"))))
						.then(Commands.literal("accept")
								.then(Commands.argument("name", StringArgumentType.greedyString()).suggests(SUGGEST_INVITED_FACTIONS).executes(ctx -> acceptInvite(ctx, StringArgumentType.getString(ctx, "name"))))))
				.then(Commands.literal("kick")
						.then(Commands.argument("target", EntityArgument.player())
								.suggests(SUGGEST_PLAYERS_WITHIN_FACTION).executes(ctx -> kickPlayer(ctx, EntityArgument.getPlayer(ctx, "target")))))
				.then(Commands.literal("leave").executes(FactionCommand::leaveFaction))
				.then(Commands.literal("list").executes(FactionCommand::listFactions))
				.then(Commands.literal("manage").executes(FactionCommand::manage))
				.then(Commands.literal("promote")
						.then(Commands.argument("target", EntityArgument.player())
								.suggests(SUGGEST_PLAYERS_WITHIN_FACTION)
								.executes(ctx -> promotePlayer(ctx, EntityArgument.getPlayer(ctx, "target")))))
		);
	}

	private static int listBeacons(CommandContext<CommandSourceStack> ctx) {
		FactionSavedData data = FactionSavedData.getSavedData(ctx.getSource().getLevel());

		Component divider = Component.literal("====== ")
				.withStyle(ChatFormatting.DARK_GRAY)
				.append(MessageUtil.Prefix.PREFIX_PLAIN)
				.append(Component.literal(" ======").withStyle(ChatFormatting.DARK_GRAY));

		Iterator<Faction> it = data.getFactions().values().iterator();
		while (it.hasNext()) {
			Faction faction = it.next();

			ctx.getSource().sendSuccess(() -> divider, false);
			ctx.getSource().sendSuccess(() -> Component.translatable(ModTranslations.DEBUG_LIST_BEACON_POSITIONS_FACTION_NAME).withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false).withBold(false)).append(faction.getName().replace("&", "§")), false);

			if (faction.getBeaconPos() == null) {
				ctx.getSource().sendSuccess(() -> Component.translatable(ModTranslations.DEBUG_LIST_BEACON_POSITIONS_POS_TEXT).withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false).withBold(false)).append(Component.translatable(ModTranslations.DEBUG_LIST_BEACON_POSITION_NULL_ERROR).withStyle(style -> style.withColor(ChatFormatting.RED).withBold(false).withItalic(false))), false);
			} else {
				ctx.getSource().sendSuccess(() -> Component.translatable(ModTranslations.DEBUG_LIST_BEACON_POSITIONS_POS_TEXT).withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false).withBold(false)).append(String.format("%s, %s, %s", faction.getBeaconPos().getX(), faction.getBeaconPos().getY(), faction.getBeaconPos().getZ())), false);
			}
			if (!it.hasNext()) {
				ctx.getSource().sendSuccess(() -> divider, false);
			}
		}
		return 1;
	}

	private static int openConfirmTransferScreen(CommandContext<CommandSourceStack> ctx, String targetUUID) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NOT_A_PLAYER));
			return 0;
		}

		NetworkHooks.openScreen(player, new SimpleMenuProvider(
						(pContainerId, pPlayerInventory, pPlayer) -> new ConfirmTransferOwnershipMenu(pContainerId, pPlayerInventory, UUID.fromString(targetUUID)),
						Component.translatable(ModTranslations.CONFIRM_TRANSFER)),
				buf -> buf.writeUUID(UUID.fromString(targetUUID)));
		return 1;
	}

	private static int openScreenDebugCommand(CommandContext<CommandSourceStack> ctx, ModScreens screen) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NOT_A_PLAYER));
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
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NOT_A_PLAYER));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());
		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NO_FACTION));
			return 0;
		}

		Faction.Rank playerRank = faction.getMembers().get(player.getUUID());
		if (playerRank != Faction.Rank.GENERALISSIMUS && playerRank != Faction.Rank.STADHOUDER) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.INSUFFICIENT_RANK));
			return 0;
		}

		NetworkHooks.openScreen(player, new SimpleMenuProvider(
				(pContainerId, pPlayerInventory, pPlayer) -> new ManageFactionMenu(pContainerId, pPlayerInventory),
				Component.literal(faction.getName().replace("&", "§"))));
		return 1;
	}

	private static int kickPlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
		return kickPlayer(ctx, target, true);
	}

	private static int kickPlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target, boolean sendMessage) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NOT_A_PLAYER));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());
		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NO_FACTION));
			return 0;
		}

		Faction.Rank playerRank = faction.getMembers().get(player.getUUID());
		if ((playerRank != Faction.Rank.GENERALISSIMUS && playerRank != Faction.Rank.STADHOUDER)) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.OWNER_KICK));
			return 0;
		}

		if (target.getUUID().equals(player.getUUID())) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.SELF_KICK));
			return 0;
		}

		if (!faction.getMembers().containsKey(target.getUUID())) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.PLAYER_NOT_IN_FACTION));
			return 0;
		}

		faction.getMembers().remove(target.getUUID());
		data.save(player.serverLevel());
		if (sendMessage && !target.getUUID().equals(player.getUUID())) {
			ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(ModTranslations.KICK_SUCCESS, target.getDisplayName().getString()), false);
		}
		return 1;
	}

	private static int promotePlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NOT_A_PLAYER));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());

		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NO_FACTION));
			return 0;
		}
		if (!faction.getMembers().containsKey(target.getUUID())) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.PLAYER_NOT_IN_FACTION_PROMOTE));
			return 0;
		}

		Faction.Rank playerRank = faction.getMembers().get(player.getUUID());
		Faction.Rank targetRank = faction.getMembers().get(target.getUUID());

		if (!Arrays.asList(playerRank.getManageableRanks()).contains(targetRank)) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.EQUAL_RANK_PROMOTE));
			return 0;
		}

		int newRankId = targetRank.getId() + 1;
		if (!Arrays.asList(playerRank.getManageableRanks()).contains(Faction.Rank.getRankById(newRankId))) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.EQUAL_RANK_PROMOTE));
			return 0;
		}

		faction.getMembers().put(target.getUUID(), Faction.Rank.getRankById(newRankId));
		data.save(player.serverLevel());
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(ModTranslations.PROMOTE_SUCCESS, target.getDisplayName(), Faction.Rank.getRankById(newRankId).getName()) , false);
		return 1;
	}

	private static int listFactions(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player = ctx.getSource().getPlayer();

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		if (data.getFactions().isEmpty()) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NO_EXISTING_FACTIONS));
			return 1;
		}

		Collection<Faction> factions = data.getFactions().values();
		List<Faction> visibleFactions = factions.stream()
				.filter(faction -> faction.getSettingValue(FactionSettings.INFO_VISIBLE.getNbtId(), BooleanFactionSetting.class))
				.toList();

		if (visibleFactions.isEmpty()) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NO_VISIBLE_FACTIONS));
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
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NO_VISIBLE_FACTIONS));
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
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NOT_A_PLAYER));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());
		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NO_FACTION));
			return 0;
		}

		if (faction.getOwner().equals(player.getUUID())) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.LEAVE_TRANSFER_OWNERSHIP));
			return 0;
		}

		faction.getMembers().remove(player.getUUID());
		data.save(player.serverLevel());
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(ModTranslations.LEAVE_SUCCESS, faction.getName().replace("&", "§")), false);
		return 1;
	}

	private static int teleportToBeacon(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NOT_A_PLAYER));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());
		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NO_FACTION));
			return 0;
		}

		BlockPos beaconPos = faction.getBeaconPos();

		if (!faction.hasBeacon()) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NO_BEACON));
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
				ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.HOME_COOLDOWN, hours, minutes, seconds));
			}
			faction.setHomeCooldown(player.getUUID(), currentTime);
		}
		data.save(player.serverLevel());
		ServerLevel overworld = Objects.requireNonNull(player.getServer()).getLevel(Level.OVERWORLD);
		player.teleportTo(overworld,
				beaconPos.getX() + 0.5,
				beaconPos.getY() + 1,
				beaconPos.getZ() + 0.5,
				player.getYRot(),
				player.getXRot());
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(ModTranslations.HOME_SUCCESS), false);
		return 1;
	}
	private static int factionInfo(CommandContext<CommandSourceStack> ctx, String factionName) {
		ServerPlayer player = ctx.getSource().getPlayer();

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionByStrippedName(factionName);
		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NO_FACTION));
			return 0;
		}

		if (!faction.getSettingValue(FactionSettings.INFO_VISIBLE.getNbtId(), BooleanFactionSetting.class)) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.PRIVATE_FACTION));
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
		ctx.getSource().sendSuccess(() -> Component.translatable(ModTranslations.FACTION_INFO_FACTION_NAME, faction.getName().replace("&" , "§")).withStyle(ChatFormatting.GRAY), false);
		ctx.getSource().sendSuccess(() -> Component.translatable(ModTranslations.FACTION_INFO_FACTION_MEMBERS, membersStr).withStyle(ChatFormatting.GRAY), false);
		//ctx.getSource().sendSuccess(() -> Component.literal("Allies: " + alliesStr).withStyle(ChatFormatting.GRAY), false);
		ctx.getSource().sendSuccess(() -> divider, false);
		return 1;
	}

	private static int invitePlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NOT_A_PLAYER));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());
		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NO_FACTION));
			return 0;
		}
		Faction.Rank playerRank = faction.getMembers().get(player.getUUID());
		if (playerRank == Faction.Rank.PRIVATE) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.INSUFFICIENT_RANK));
			return 0;
		}
		if (data.getFactionFromPlayer(target.getUUID()) != null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.TARGET_PLAYER_IN_FACTION));
			return 0;
		}

		if (faction.getMembers().size() >= ModConfigs.commonConfig.maxMembers.get()) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.MAX_MEMBERS));
			return 0;
		}

		InviteManager.invite(target.getUUID(), faction.getName());
		data.save(player.serverLevel());
		target.sendSystemMessage(MessageUtil.Prefix.success(ModTranslations.INVITED_PLAYER_MESSAGE, faction.getName().replace("&", "§"), faction.getName().replace("&", "§")), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(ModTranslations.INVITING_PLAYER_MESSAGE, target.getName().getString()), false);
		return 1;
	}

	private static int disbandFaction(CommandContext<CommandSourceStack> ctx, String factionName) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NOT_A_PLAYER));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionByStrippedName(factionName);

		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.TARGETED_FACTION_DOESNT_EXIST));
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
				member.sendSystemMessage(MessageUtil.Prefix.error(ModTranslations.FACTION_DISBANDED_BY_ADMIN));
				member.setRespawnPosition(Level.OVERWORLD, null, 0.0F, true, false);
				kickPlayer(ctx, member, false);
			}
		}

		data.removeFaction(faction, player.serverLevel());
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(ModTranslations.FACTION_DISBAND_SUCCESS_ADMIN, faction.getName().replace("&", "§")), false);
		return 1;
	}

	private static int disbandFaction(CommandContext<CommandSourceStack> ctx) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NOT_A_PLAYER));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());

		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NO_FACTION));
			return 0;
		}

		Faction.Rank playerRank = faction.getMembers().get(player.getUUID());
		boolean isLeader = playerRank == Faction.Rank.GENERALISSIMUS || playerRank == Faction.Rank.STADHOUDER;
		boolean isOwner = faction.getOwner().equals(player.getUUID());
		if (!isLeader && !isOwner) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.FACTION_OWNER_CAN_DISBAND));
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
					member.sendSystemMessage(MessageUtil.Prefix.error(ModTranslations.FACTION_DISBANDED_MEMBER));
					kickPlayer(ctx, member, true);
				}
				member.setRespawnPosition(Level.OVERWORLD, null, 0.0F, true, false);
			}
		}

		data.removeFaction(faction, player.serverLevel());
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(ModTranslations.FACTION_DISBAND_SUCCESS), false);
		return 1;
	}

	private static int demotePlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NOT_A_PLAYER));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());
		Faction faction = data.getFactionFromPlayer(player.getUUID());

		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NO_FACTION));
			return 0;
		}
		if (!faction.getMembers().containsKey(target.getUUID())) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.PLAYER_NOT_IN_FACTION));
			return 0;
		}

		Faction.Rank playerRank = faction.getMembers().get(player.getUUID());
		Faction.Rank targetRank = faction.getMembers().get(target.getUUID());

		if (!Arrays.asList(playerRank.getManageableRanks()).contains(targetRank)) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.INSUFFICIENT_RANK));
			return 0;
		}

		int newRankId = targetRank.getId() - 1;
		if (newRankId < 0) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.PLAYER_AT_LOWEST_RANK));
			return 0;
		}

		if (!Arrays.asList(playerRank.getManageableRanks()).contains(Faction.Rank.getRankById(newRankId))) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.INSUFFICIENT_RANK));
			return 0;
		}

		faction.getMembers().put(target.getUUID(), Faction.Rank.getRankById(newRankId));
		data.save(player.serverLevel());
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(ModTranslations.DEMOTE_SUCCESS, target.getDisplayName(), Faction.Rank.getRankById(newRankId).getName()), false);
		return 1;
	}

	private static int createFaction(CommandContext<CommandSourceStack> ctx, String name) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NOT_A_PLAYER));
			return 0;
		}

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());

		String formattedName = name.replace("&", "§");
		String strippedName = formattedName.replaceAll("§[0-9a-fk-or]", "");
		for (Faction faction : data.getFactions().values()) {
			String existingNamesStripped = faction.getName().replaceAll("§[0-9a-fk-or]", "");
			if (existingNamesStripped.equalsIgnoreCase(strippedName)) {
				ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NAME_TAKEN));
				return 0;
			}
		}

		List<AbstractFactionSetting<?, ?>> settings = Faction.createDefaultSettings();

		Faction faction = new Faction(formattedName, player.getUUID(), settings);
		faction.setOwner(player.getUUID());

		ItemStack beacon = new ItemStack(Items.BEACON);
		beacon.getOrCreateTag().putBoolean("respawn_beacon", true);
		beacon.setHoverName(Component.translatable(ModTranslations.RESPAWN_BEACON_NAME)
				.withStyle(style -> style
						.withBold(true)
						.withColor(ChatFormatting.RED)
						.withItalic(false)));
		CompoundTag display = beacon.getOrCreateTagElement("display");
		ListTag lore = new ListTag();
		lore.add(StringTag.valueOf(Component.Serializer.toJson(
				Component.translatable(ModTranslations.RESPAWN_BEACON_LORE_LINE_1)
						.withStyle(style -> style
								.withColor(ChatFormatting.GRAY)
								.withItalic(false))
		)));
		lore.add(StringTag.valueOf(Component.Serializer.toJson(
				Component.translatable(ModTranslations.RESPAWN_BEACON_LORE_LINE_2)
						.withStyle(style -> style
								.withColor(ChatFormatting.GRAY)
								.withItalic(false))
		)));

		lore.add(StringTag.valueOf(Component.Serializer.toJson(
				Component.translatable(ModTranslations.RESPAWN_BEACON_LORE_LINE_3)
						.withStyle(style -> style
								.withBold(false)
								.withItalic(false)
								.withColor(ChatFormatting.GRAY))
		)));

		display.put("Lore", lore);
		player.getInventory().add(beacon);
		data.addFaction(faction, player.serverLevel());
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(ModTranslations.CREATE_FACTION_SUCCESS, formattedName), false);
		return 1;
	}

	private static int acceptInvite(CommandContext<CommandSourceStack> ctx, String name) {
		ServerPlayer player = ctx.getSource().getPlayer();
		if (player == null) return 0;

		FactionSavedData data = FactionSavedData.getSavedData(player.serverLevel());

		if (data.getFactionFromPlayer(player.getUUID()) != null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.PLAYER_IN_FACTION));
			return 0;
		}

		Faction faction = data.getFactionByStrippedName(name);

		if (faction == null) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.FACTION_NOT_EXISTING));
			return 0;
		}

		if (faction.getMembers().size() >= ModConfigs.commonConfig.maxMembers.get()) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.MAX_MEMBERS));
			return 0;
		}

		if (!InviteManager.hasInvite(player.getUUID(), name)) {
			ctx.getSource().sendFailure(MessageUtil.Prefix.error(ModTranslations.NO_INVITES));
			return 0;
		}

		faction.getMembers().put(player.getUUID(), Faction.Rank.PRIVATE);
		InviteManager.cancelInvite(player.getUUID());
		data.save(player.serverLevel());

		Objects.requireNonNull(Objects.requireNonNull(ctx.getSource().getPlayer().getServer()).getPlayerList().getPlayer(faction.getOwner())).sendSystemMessage(MessageUtil.Prefix.info(ModTranslations.ACCEPT_SUCCESS_FACTION_OWNER, player.getGameProfile().getName()));
		ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(ModTranslations.ACCEPT_SUCCESS, name.replace("&", "§")), false);
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
		ctx.getSource().sendSuccess(() -> MessageUtil.helpDescription("/faction create <name> - ", ModTranslations.HELP_COMMAND_CREATE), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.helpDescription("/faction demote <player> - ", ModTranslations.HELP_COMMAND_DEMOTE), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.helpDescription("/faction disband - ", ModTranslations.HELP_COMMAND_DISBAND), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.helpDescription("/faction disband <name> - ", ModTranslations.HELP_COMMAND_DISBAND_BY_ADMIN), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.helpDescription("/faction help - ", ModTranslations.HELP_COMMAND_HELP), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.helpDescription("/faction home - ", ModTranslations.HELP_COMMAND_HOME), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.helpDescription("/faction info <name> - ", ModTranslations.HELP_COMMAND_INFO), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.helpDescription("/faction invite <player> - ", ModTranslations.HELP_COMMAND_INVITE), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.helpDescription("/faction invite accept <name> - ", ModTranslations.HELP_COMMAND_INVITE_ACCEPT), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.helpDescription("/faction kick - ", ModTranslations.HELP_COMMAND_KICK), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.helpDescription("/faction leave - ", ModTranslations.HELP_COMMAND_LEAVE), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.helpDescription("/faction list - ", ModTranslations.HELP_COMMAND_LIST), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.helpDescription("/faction manage - ", ModTranslations.HELP_COMMAND_MANAGE), false);
		ctx.getSource().sendSuccess(() -> MessageUtil.helpDescription("/faction promote <name> - ", ModTranslations.HELP_COMMAND_PROMOTE), false);
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
