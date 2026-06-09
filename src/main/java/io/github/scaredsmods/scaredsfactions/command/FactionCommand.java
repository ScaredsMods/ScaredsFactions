package io.github.scaredsmods.scaredsfactions.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.scaredsmods.scaredsfactions.ModConfigs;
import io.github.scaredsmods.scaredsfactions.ModPermissions;
import io.github.scaredsmods.scaredsfactions.compat.luckperms.LuckPermsAPICompat;
import io.github.scaredsmods.scaredsfactions.config.LanguageOptions;
import io.github.scaredsmods.scaredsfactions.faction.Faction;
import io.github.scaredsmods.scaredsfactions.faction.InviteManager;
import io.github.scaredsmods.scaredsfactions.util.MessageUtil;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class FactionCommand {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_PLAYERS_WITHOUT_FACTION = (ctx, builder) -> {
        ServerPlayer player = ctx.getSource().getPlayer();
        ServerLevel level = player.serverLevel();
        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(level);
        level.getServer().getPlayerList().getPlayers().stream()
                .filter(p -> data.getFactionFromPlayer(p.getUUID()) == null)
                .forEach(p -> builder.suggest(p.getDisplayName().getString()));
        return builder.buildFuture();
    };

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_PLAYERS_WITHIN_FACTION = (ctx, builder) -> {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return builder.buildFuture();
        ServerLevel level = player.serverLevel();
        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(level);
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
        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(ctx.getSource().getLevel());
        data.getFactions().keySet().forEach(name -> {
            String displayName = name.replaceAll("§[0-9a-fk-or]", "");
            builder.suggest(name, Component.literal(displayName));
        });
        return builder.buildFuture();
    };
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_ALLIES = (ctx, builder) -> {
        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(ctx.getSource().getLevel());
        data.getAlliedFactions().keySet().forEach(builder::suggest);
        return builder.buildFuture();
    };

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_INVITED_FACTIONS = (ctx, builder) -> {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player != null)  {
            String invite = InviteManager.getPendingInvite(player.getUUID());
            if (invite != null) {
                builder.suggest(invite);
            }
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
                .then(Commands.literal("demote")
                        .then(Commands.argument("target", EntityArgument.player())
                                .suggests(SUGGEST_PLAYERS_WITHIN_FACTION)
                                .executes(ctx -> demotePlayer(ctx, EntityArgument.getPlayer(ctx, "target")))))
                .then(Commands.literal("disband")
                        .executes(FactionCommand::disbandFaction)
                        .then(Commands.argument("name", StringArgumentType.string())
                            .requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_USE_DISBAND_BY_NAME) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL))
                            .suggests(SUGGEST_FACTION_NAMES)
                            .executes(ctx -> disbandFaction(ctx, StringArgumentType.getString(ctx, "name"))))
                        .then(Commands.argument("name", StringArgumentType.word())
                            .requires(source -> LuckPermsAPICompat.hasPermission(source, ModPermissions.CAN_USE_DISBAND_BY_NAME) || LuckPermsAPICompat.hasPermission(source, ModPermissions.ALL))
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
                                .then(Commands.argument("name", StringArgumentType.word()).suggests(SUGGEST_INVITED_FACTIONS).executes(ctx -> acceptInvite(ctx, StringArgumentType.getString(ctx, "factionName"))))
                                .then(Commands.argument("name", StringArgumentType.string()).suggests(SUGGEST_INVITED_FACTIONS).executes(ctx -> acceptInvite(ctx, StringArgumentType.getString(ctx, "factionName"))))
                                .then(Commands.argument("name", StringArgumentType.greedyString()).suggests(SUGGEST_INVITED_FACTIONS).executes(ctx -> acceptInvite(ctx, StringArgumentType.getString(ctx, "factionName"))))))
                .then(Commands.literal("kick")
                        .then(Commands.argument("target", EntityArgument.player())
                                .suggests(SUGGEST_PLAYERS_WITHIN_FACTION).executes(ctx -> kickPlayer(ctx, EntityArgument.getPlayer(ctx, "target")))))
                .then(Commands.literal("leave").executes(FactionCommand::leaveFaction))
                .then(Commands.literal("list").executes(FactionCommand::listFactions))
                .then(Commands.literal("promote")
                        .then(Commands.argument("target", EntityArgument.player())
                                .suggests(SUGGEST_PLAYERS_WITHIN_FACTION)
                                .executes(ctx -> promotePlayer(ctx, EntityArgument.getPlayer(ctx, "target")))))
        );
    }

    private static int kickPlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You must be a player to execute this command!"));
            return 0;
        }
        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());
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
        data.setDirty();
        ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(String.format("Successfully kicked %s from the faction!", target.getDisplayName().getString())), true);
        return 1;
    }

    private static int promotePlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
            return 0;
        }

        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());
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

        if (!Arrays.asList(playerRank.getPromotableRanks()).contains(targetRank)) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You cannot promote this player!"));
            return 0;
        }

        int newRankId = targetRank.getId() + 1;
        if (!Arrays.asList(playerRank.getPromotableRanks()).contains(Faction.Rank.getRankById(newRankId))) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You cannot promote this player to that rank!"));
            return 0;
        }

        faction.getMembers().put(target.getUUID(), Faction.Rank.getRankById(newRankId));
        data.setDirty();
        return 1;
    }

    private static int listFactions(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
            return 0;
        }
        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());
        if (data.getFactions().isEmpty()) {
            ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.error("No factions exist yet!"), false);
            return 1;
        }

        Component divider = Component.literal("====== ")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(MessageUtil.Prefix.PREFIX_PLAIN)
                .append(Component.literal(" ======").withStyle(ChatFormatting.DARK_GRAY));

        String factionList = data.getFactions().values().stream()
                .filter(Faction::isInfoVisible)
                .map(faction -> faction.getName().replace("&", "§") + "§r (" + faction.getMembers().size() + " members)")
                .collect(Collectors.joining(", "));

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

        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());
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
        data.setDirty();
        ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success("You left " + faction.getName() + "!"), true);
        return 1;
    }

    private static int teleportToBeacon(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
            return 0;
        }

        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());
        Faction faction = data.getFactionFromPlayer(player.getUUID());
        if (faction == null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be in a faction to use this command!"));
            return 0;
        }

        BlockPos beaconPos = data.getBeaconPosition(faction.getName());

        if (beaconPos == null || !data.hasBeacon(faction.getName())) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("Your faction doesn't have their beacon placed down yet!"));
            return 0;
        }

        long currentTime = System.currentTimeMillis() / 1000;
        if (ModConfigs.commonConfig.enableHomeCommandCooldown.get()) {
            long cooldownSeconds = ModConfigs.commonConfig.homeCommandCooldown.get();
            long lastUsed = data.getHomeCooldown(player.getUUID());
            long remainingTime = (lastUsed + cooldownSeconds) - currentTime;
            if (remainingTime > 0) {
                long hours = remainingTime / 3600;
                long minutes = (remainingTime % 3600) / 60;
                long seconds = remainingTime % 60;
                ctx.getSource().sendFailure(MessageUtil.Prefix.error(String.format("You must wait another %s:%s:%s before using this command again!", hours, minutes, seconds)));
            }
            data.setHomeCooldown(player.getUUID(), currentTime);
        }
        data.setDirty();
        player.teleportTo(beaconPos.getX() + 0.5, beaconPos.getY() + 1,  beaconPos.getZ() + 0.5);
        ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success("Successfully teleported to your faction's beacon!"), true);
        return 1;
    }

    private static int factionInfo(CommandContext<CommandSourceStack> ctx, String factionName) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
            return 0;
        }
        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());
        Faction faction = data.getFaction(factionName);
        if (faction == null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("That faction does not exist!"));
            return 0;
        }

        if (!faction.isInfoVisible()) {
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
        ctx.getSource().sendSuccess(() -> Component.literal("Allies: " + alliesStr).withStyle(ChatFormatting.GRAY), false);
        ctx.getSource().sendSuccess(() -> divider, false);
        return 1;
    }

    private static int invitePlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
            return 0;
        }
        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());
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

        InviteManager.invite(target.getUUID(), faction.getName());
        data.setDirty();
        target.sendSystemMessage(MessageUtil.Prefix.success("You have been invited to join " + faction.getName() + ". Use /faction join " + faction.getName() + " to accept."), false);
        ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.formattedMessage("Invited " + target.getName().getString() + " to your faction.", ChatFormatting.GOLD), false);
        return 1;
    }

    private static int disbandFaction(CommandContext<CommandSourceStack> ctx, String factionName) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
            return 0;
        }

        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());
        Faction faction = data.getFaction(factionName);

        if (faction == null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("This faction doesn't exist!"));
            return 0;
        }

        ServerLevel level = ctx.getSource().getLevel();
        BlockPos beaconPos = data.getBeaconPosition(faction.getName());
        data.removeFaction(faction);

        if (beaconPos != null) {
            level.destroyBlock(beaconPos, false);
            data.removeBeacon(faction.getName());
        }
        data.setDirty();
        for (UUID memberUUID : faction.getMembers().keySet()) {
            ServerPlayer member = ctx.getSource().getServer().getPlayerList().getPlayer(memberUUID);
            if (member != null) {
                member.sendSystemMessage(MessageUtil.Prefix.error("Your faction has been disbanded by an admin!"));
                member.setRespawnPosition(Level.OVERWORLD, null, 0.0F, true, false);
                player.setRespawnPosition(Level.OVERWORLD, null, 0.0F, true, false);
            }
        }
        ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.error("Faction " + faction.getName().replace("&", "§") + "§r has been disbanded!"), true);
        return 1;
    }


    private static int disbandFaction(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
            return 0;
        }
        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());
        Faction faction = data.getFactionFromPlayer(player.getUUID());

        if (faction == null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("This faction doesn't exist!"));
            return 0;
        }

        Faction.Rank playerRank = faction.getMembers().get(player.getUUID());
        if (playerRank != Faction.Rank.GENERALISSIMUS && playerRank != Faction.Rank.STADHOUDER) {
            if (!player.getUUID().equals(faction.getOwner())) {
                ctx.getSource().sendFailure(MessageUtil.Prefix.error("Only the owner can disband the faction!"));
                return 0;
            }
        }
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos beaconPos = data.getBeaconPosition(faction.getName());
        if (beaconPos != null) {
            level.destroyBlock(beaconPos, false);
            data.removeBeacon(faction.getName());
        }
        data.removeFaction(faction);
        data.setDirty();
        for (UUID memberUUID : faction.getMembers().keySet()) {
            ServerPlayer member = ctx.getSource().getServer().getPlayerList().getPlayer(memberUUID);
            if (member != null) {
                member.sendSystemMessage(MessageUtil.Prefix.error("Your faction has been disbanded by an admin!"));
                member.setRespawnPosition(Level.OVERWORLD, null, 0.0F, true, false);
                player.setRespawnPosition(Level.OVERWORLD, null, 0.0F, true, false);
            }
        }
        ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.error("Faction " + faction.getName().replace("&", "§") + "§r has been disbanded!"), true);
        return 1;
    }

    private static int demotePlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
            return 0;
        }

        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());
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

        if (!Arrays.asList(playerRank.getPromotableRanks()).contains(targetRank)) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You cannot demote this player!"));
            return 0;
        }

        int newRankId = targetRank.getId() - 1;
        if (newRankId < 0) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("This player is already the lowest rank!"));
            return 0;
        }

        if (!Arrays.asList(playerRank.getPromotableRanks()).contains(Faction.Rank.getRankById(newRankId))) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You cannot demote this player to that rank!"));
            return 0;
        }

        faction.getMembers().put(target.getUUID(), Faction.Rank.getRankById(newRankId));
        data.setDirty();
        return 1;
    }

    private static int createFaction(CommandContext<CommandSourceStack> ctx, String name) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You need to be a player to use this command!"));
            return 0;
        }
        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());

        String formattedName = name.replace("&", "§");
        String strippedName = formattedName.replaceAll("§[0-9a-fk-or]", "");
        for (Faction faction : data.getFactions().values()) {
            String existingNamesStripped = faction.getName().replaceAll("§[0-9a-fk-or]", "");
            if (existingNamesStripped.equalsIgnoreCase(strippedName)) {
                ctx.getSource().sendFailure(MessageUtil.Prefix.error("This faction already exists! Please choose another name!"));
                return 0;
            }
        }

        Faction faction = new Faction(name, player.getUUID(), true);
        Faction.Rank rank = (ModConfigs.commonConfig.highestRank.get() == LanguageOptions.USE_STADHOUDER) ? Faction.Rank.STADHOUDER : Faction.Rank.GENERALISSIMUS;
        faction.getMembers().put(player.getUUID(), rank);
        data.addFaction(faction);

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
        data.setDirty();
        ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success("Faction ")
                .copy()
                .append(Component.literal(formattedName))
                .append(Component.literal(" created!").withStyle(ChatFormatting.GREEN)), true);
        return 1;
    }

    private static int acceptInvite(CommandContext<CommandSourceStack> ctx, String factionName) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        Faction.FactionSavedData data = Faction.FactionSavedData.getSavedData(player.serverLevel());

        if (data.getFactionFromPlayer(player.getUUID()) != null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You are already in a faction! Leave your current faction first before joining a new one!"));
            return 0;
        }

        if (!InviteManager.hasInvite(player.getUUID(), factionName)) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("You don't have any invites!"));
            return 0;
        }
        Faction faction = data.getFaction(factionName);
        if (faction == null) {
            ctx.getSource().sendFailure(MessageUtil.Prefix.error("This faction does not exist! Are you sure you spelled it correctly?"));
            return 0;
        }

        faction.getMembers().put(player.getUUID(), Faction.Rank.PRIVATE);
        InviteManager.cancelInvite(player.getUUID());
        data.setDirty();
        ctx.getSource().sendSuccess(() -> MessageUtil.Prefix.success(String.format("You successfully joined %s!", factionName)), true);
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
        ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction promote <name> - Promotes a player to a new rank. (WIP)"), false);
        //ctx.getSource().sendSuccess(() -> MessageUtil.info("/faction unally <name> - Terminate the formal alliance"), false);
        ctx.getSource().sendSuccess(() -> divider, false);
        return 1;
    }
}
