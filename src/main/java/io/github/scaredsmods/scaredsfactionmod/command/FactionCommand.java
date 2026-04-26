package io.github.scaredsmods.scaredsfactionmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.scaredsmods.scaredsfactionmod.faction.Faction;
import io.github.scaredsmods.scaredsfactionmod.faction.FactionRank;
import io.github.scaredsmods.scaredsfactionmod.faction.InviteManager;
import io.github.scaredsmods.scaredsfactionmod.faction.PersistentData;
import io.github.scaredsmods.scaredsfactionmod.util.PrefixUtil;
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

import java.util.UUID;

public class FactionCommand {


    private static final SuggestionProvider<CommandSourceStack> SUGGEST_FACTIONS =
            (ctx, builder) -> {
                PersistentData data = PersistentData.get(ctx.getSource().getLevel());
                data.getFactions().keySet().forEach(builder::suggest);
                return builder.buildFuture();
            };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ALLIES =
            (ctx, builder) -> {
                PersistentData data = PersistentData.get(ctx.getSource().getLevel());
                ServerPlayer player = ctx.getSource().getPlayer();
                if (player != null) {
                    Faction faction = data.getFactionByPlayer(player.getUUID());
                    if (faction != null) {
                        faction.getAllies().forEach(builder::suggest);
                    }
                }
                return builder.buildFuture();
            };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_INVITED_FACTIONS =
            (ctx, builder) -> {
                ServerPlayer player = ctx.getSource().getPlayer();
                if (player != null) {
                    String invite = InviteManager.getInvite(player.getUUID());
                    if (invite != null) {
                        builder.suggest(invite);
                    }
                }
                return builder.buildFuture();
            };


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("faction")
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> createFaction(ctx, StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("disband")
                        .executes(FactionCommand::disbandFaction)
                        .then(Commands.argument("name", StringArgumentType.word())
                                .requires(source -> source.hasPermission(4))
                                .suggests(SUGGEST_FACTIONS)
                                .executes(ctx -> disbandFactionByName(ctx, StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> invitePlayer(ctx, EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("join")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests(SUGGEST_INVITED_FACTIONS)
                                .executes(ctx -> joinFaction(ctx, StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("leave")
                        .executes(FactionCommand::leaveFaction))
                .then(Commands.literal("list")
                        .executes(FactionCommand::listFactions))
                .then(Commands.literal("info")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests(SUGGEST_FACTIONS)
                                .executes(ctx -> factionInfo(ctx, StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("ally")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests(SUGGEST_FACTIONS)
                                .executes(ctx -> allyFaction(ctx, StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("unally")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests(SUGGEST_ALLIES)
                                .executes(ctx -> unallyFaction(ctx, StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("promote")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> promotePlayer(ctx, EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("demote")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> demotePlayer(ctx, EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("home")
                        .executes(FactionCommand::teleportToBeacon))
        );
    }

    private static int teleportToBeacon(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        PersistentData data = PersistentData.get(ctx.getSource().getLevel());
        Faction faction = data.getFactionByPlayer(player.getUUID());

        if (faction == null) {
            ctx.getSource().sendFailure(PrefixUtil.error("You aren't in a faction! Join a faction to use this command!"));
            return 0;
        }
        BlockPos beaconPos = data.getBeaconPos(faction.getName());

        if (beaconPos == null) {
            ctx.getSource().sendFailure(PrefixUtil.error("Your faction hasn't placed down their beacon yet! Make sure the beacon has been placed down before using this command!"));
            return 0;
        }

        player.teleportTo(beaconPos.getX() + 0.5, beaconPos.getY() + 1, beaconPos.getZ() + 0.5);
        ctx.getSource().sendSuccess(() -> PrefixUtil.success("Successfully teleported to your faction's beacon!"), false);
        return 1;
    }


    private static int createFaction(CommandContext<CommandSourceStack> ctx, String name)  {
        ServerPlayer player = ctx.getSource().getPlayer();
        PersistentData data = PersistentData.get(ctx.getSource().getLevel());

        if (data.getFaction(name) != null) {
            ctx.getSource().sendFailure(PrefixUtil.error("A faction with that name already exists!"));
            return 0;
        }
        if (data.getFactionByPlayer(player.getUUID()) != null) {
            ctx.getSource().sendFailure(PrefixUtil.error("You are already in a faction!"));
            return 0;
        }

        Faction faction = new Faction(name, player.getUUID());
        faction.getMembers().add(player.getUUID());
        faction.setOwner(player.getUUID());
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

        ctx.getSource().sendSuccess(() -> PrefixUtil.success("Faction " + name + " created!"), false);
        return 1;
    }

    private static int disbandFaction(CommandContext<CommandSourceStack> ctx)   {
        ServerPlayer player = ctx.getSource().getPlayer();
        PersistentData data = PersistentData.get(ctx.getSource().getLevel());
        Faction faction = data.getFactionByPlayer(player.getUUID());

        if (faction == null) {
            ctx.getSource().sendFailure(PrefixUtil.error("You are not in a faction!"));
            return 0;
        }
        if (!faction.getOwner().equals(player.getUUID())) {
            ctx.getSource().sendFailure(PrefixUtil.error("Only the owner can disband the faction!"));
            return 0;
        }

        for (UUID memberUUID : faction.getMembers()) {
            ServerPlayer member = ctx.getSource().getServer().getPlayerList().getPlayer(memberUUID);
            if (member != null) {
                member.sendSystemMessage(PrefixUtil.error("Your faction has been disbanded!"));
            }
        }
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos beaconPos = data.getBeaconPos(faction.getName());
        if (beaconPos != null) {
            level.destroyBlock(beaconPos, false);
        }
        data.removeFaction(faction.getName());
        return 1;
    }

    private static int disbandFactionByName(CommandContext<CommandSourceStack> ctx, String name) {
        PersistentData data = PersistentData.get(ctx.getSource().getLevel());
        Faction faction = data.getFaction(name);

        if (faction == null) {
            ctx.getSource().sendFailure(PrefixUtil.error("That faction does not exist!"));
            return 0;
        }

        for (UUID memberUUID : faction.getMembers()) {
            ServerPlayer member = ctx.getSource().getServer().getPlayerList().getPlayer(memberUUID);
            if (member != null) {
                member.sendSystemMessage(PrefixUtil.error("Your faction has been disbanded by an admin!"));
            }
        }
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos beaconPos = data.getBeaconPos(faction.getName());
        if (beaconPos != null) {
            level.destroyBlock(beaconPos, false);
        }
        data.removeFaction(faction.getName());
        ctx.getSource().sendSuccess(() -> PrefixUtil.error("Faction " + name + " has been disbanded!"), false);
        return 1;
    }

    private static int invitePlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target)   {
        ServerPlayer player = ctx.getSource().getPlayer();
        PersistentData data = PersistentData.get(ctx.getSource().getLevel());
        Faction faction = data.getFactionByPlayer(player.getUUID());

        if (faction == null) {
            ctx.getSource().sendFailure(PrefixUtil.error("You are not in a faction!"));
            return 0;
        }

        FactionRank rank = faction.getRanks().get(player.getUUID());
        if (rank == FactionRank.PRIVATE) {
            ctx.getSource().sendFailure(PrefixUtil.error("You don't have permission to invite players!"));
            return 0;
        }
        if (data.getFactionByPlayer(target.getUUID()) != null) {
            ctx.getSource().sendFailure(PrefixUtil.error("That player is already in a faction!"));
            return 0;
        }

        InviteManager.addInvite(target.getUUID(), faction.getName());
        target.sendSystemMessage(PrefixUtil.success("You have been invited to join " + faction.getName() + ". Use /faction join " + faction.getName() + " to accept."), false);
        ctx.getSource().sendSuccess(() -> PrefixUtil.formattedMessage("Invited " + target.getName().getString() + " to your faction.", ChatFormatting.GOLD), false);
        return 1;
    }

    private static int joinFaction(CommandContext<CommandSourceStack> ctx, String name)   {
        ServerPlayer player = ctx.getSource().getPlayer();
        PersistentData data = PersistentData.get(ctx.getSource().getLevel());

        if (data.getFactionByPlayer(player.getUUID()) != null) {
            ctx.getSource().sendFailure(PrefixUtil.error("You are already in a faction! Leave your current faction first before joining another faction!"));
            return 0;
        }
        if (!InviteManager.hasInvite(player.getUUID(), name)) {
            ctx.getSource().sendFailure(PrefixUtil.error("You don't have an invite to that faction!"));
            return 0;
        }

        Faction faction = data.getFaction(name);
        if (faction == null) {
            ctx.getSource().sendFailure(PrefixUtil.error("That faction doesn't exist!"));
            return 0;
        }

        faction.getMembers().add(player.getUUID());
        faction.getRanks().put(player.getUUID(), FactionRank.PRIVATE);
        InviteManager.removeInvite(player.getUUID());
        data.setDirty();

        ctx.getSource().sendSuccess(() -> PrefixUtil.success("You joined " + name + "!"), false);
        return 1;
    }

    private static int leaveFaction(CommandContext<CommandSourceStack> ctx)   {
        ServerPlayer player = ctx.getSource().getPlayer();
        PersistentData data = PersistentData.get(ctx.getSource().getLevel());
        Faction faction = data.getFactionByPlayer(player.getUUID());

        if (faction == null) {
            ctx.getSource().sendFailure(PrefixUtil.error("You are not in a faction! Join a faction first before executing this command!"));
            return 0;
        }
        if (faction.getOwner().equals(player.getUUID())) {
            ctx.getSource().sendFailure(PrefixUtil.error("You are the owner of this faction! Disband the faction or transfer ownership first!"));
            return 0;
        }

        faction.getMembers().remove(player.getUUID());
        faction.getRanks().remove(player.getUUID());
        data.setDirty();

        ctx.getSource().sendSuccess(() -> PrefixUtil.success("You left " + faction.getName() + "!"), false);
        return 1;
    }

    private static int listFactions(CommandContext<CommandSourceStack> ctx) {
        PersistentData data = PersistentData.get(ctx.getSource().getLevel());

        if (data.getFactions().isEmpty()) {
            ctx.getSource().sendSuccess(() -> PrefixUtil.error("No factions exist yet!"), false);
            return 1;
        }

        Component divider = Component.literal("====== ")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(PrefixUtil.PREFIX_PLAIN)
                .append(Component.literal(" ======").withStyle(ChatFormatting.DARK_GRAY));

        StringBuilder sb = new StringBuilder("\n Factions: ");
        for (Faction faction : data.getFactions().values()) {
            sb.append("- ").append(faction.getName()).append(" (").append(faction.getMembers().size()).append(" members)\n");
        }
        ctx.getSource().sendSuccess(() -> divider, false);
        ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()).withStyle(ChatFormatting.GRAY), false);
        ctx.getSource().sendSuccess(() -> divider, false);
        return 1;
    }

    private static int factionInfo(CommandContext<CommandSourceStack> ctx, String name) {
        PersistentData data = PersistentData.get(ctx.getSource().getLevel());
        Faction faction = data.getFaction(name);

        if (faction == null) {
            ctx.getSource().sendFailure(PrefixUtil.error("That faction does not exist!"));
            return 0;
        }

        Component divider = Component.literal("====== ")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(PrefixUtil.PREFIX_PLAIN)
                .append(Component.literal(" ======").withStyle(ChatFormatting.DARK_GRAY));

        StringBuilder members = new StringBuilder();
        for (UUID memberUUID : faction.getMembers()) {
            FactionRank rank = faction.getRanks().get(memberUUID);
            String memberName = ctx.getSource().getServer().getProfileCache()
                    .get(memberUUID)
                    .map(profile -> profile.getName())
                    .orElse("Unknown Player");
            members.append(memberName).append(" (").append(rank).append("), ");
        }
        String membersStr = !members.isEmpty() ? members.substring(0, members.length() - 2) : "None";
        String alliesStr = faction.getAllies().isEmpty() ? "None" : String.join(", ", faction.getAllies());

        ctx.getSource().sendSuccess(() -> divider, false);
        ctx.getSource().sendSuccess(() -> Component.literal("Faction: " + faction.getName()).withStyle(ChatFormatting.GRAY), false);
        ctx.getSource().sendSuccess(() -> Component.literal("Members: " + membersStr).withStyle(ChatFormatting.GRAY), false);
        ctx.getSource().sendSuccess(() -> Component.literal("Allies: " + alliesStr).withStyle(ChatFormatting.GRAY), false);
        ctx.getSource().sendSuccess(() -> divider, false);
        return 1;
    }

    private static int allyFaction(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        PersistentData data = PersistentData.get(ctx.getSource().getLevel());
        Faction playerFaction = data.getFactionByPlayer(player.getUUID());

        if (playerFaction == null) {
            ctx.getSource().sendFailure(PrefixUtil.error("You are not in a faction!"));
            return 0;
        }
        if (!playerFaction.getOwner().equals(player.getUUID())) {
            ctx.getSource().sendFailure(PrefixUtil.error("Only the owner can manage allies!"));
            return 0;
        }

        Faction targetFaction = data.getFaction(name);
        if (targetFaction == null) {
            ctx.getSource().sendFailure(PrefixUtil.error("That faction does not exist!"));
            return 0;
        }
        if (playerFaction.getAllies().contains(name)) {
            ctx.getSource().sendFailure(PrefixUtil.error("You are already allied with that faction!"));
            return 0;
        }

        playerFaction.getAllies().add(name);
        targetFaction.getAllies().add(playerFaction.getName());
        data.setDirty();

        ctx.getSource().sendSuccess(() -> PrefixUtil.success("You are now allied with " + name + "!"), false);
        return 1;
    }

    private static int unallyFaction(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        PersistentData data = PersistentData.get(ctx.getSource().getLevel());
        Faction playerFaction = data.getFactionByPlayer(player.getUUID());

        if (playerFaction == null) {
            ctx.getSource().sendFailure(PrefixUtil.error("You are not in a faction!"));
            return 0;
        }
        if (!playerFaction.getOwner().equals(player.getUUID())) {
            ctx.getSource().sendFailure(PrefixUtil.error("Only the owner can manage allies!"));
            return 0;
        }

        Faction targetFaction = data.getFaction(name);
        if (targetFaction == null) {
            ctx.getSource().sendFailure(PrefixUtil.error("That faction does not exist!"));
            return 0;
        }

        playerFaction.getAllies().remove(name);
        targetFaction.getAllies().remove(playerFaction.getName());
        data.setDirty();

        ctx.getSource().sendSuccess(() -> PrefixUtil.error("You are no longer allied with " + name + "!"), false);
        return 1;
    }

    private static int promotePlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        PersistentData data = PersistentData.get(ctx.getSource().getLevel());
        Faction faction = data.getFactionByPlayer(player.getUUID());

        if (faction == null) {
            ctx.getSource().sendFailure(PrefixUtil.error("You are not in a faction!"));
            return 0;
        }
        if (!faction.getMembers().contains(target.getUUID())) {
            ctx.getSource().sendFailure(PrefixUtil.error("That player is not in your faction!"));
            return 0;
        }

        FactionRank playerRank = faction.getRanks().get(player.getUUID());
        FactionRank targetRank = faction.getRanks().get(target.getUUID());

        if (playerRank.ordinal() >= targetRank.ordinal()) {
            ctx.getSource().sendFailure(PrefixUtil.error("You cannot promote this player!"));
            return 0;
        }
        if (targetRank == FactionRank.SERGEANT) {
            ctx.getSource().sendFailure(PrefixUtil.error("This player is already the highest promotable rank!"));
            return 0;
        }

        faction.getRanks().put(target.getUUID(), FactionRank.SERGEANT);
        data.setDirty();

        ctx.getSource().sendSuccess(() -> PrefixUtil.success(target.getName().getString() + " has been promoted to Sergeant!"), false);
        return 1;
    }

    private static int demotePlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        ServerPlayer player = ctx.getSource().getPlayer();
        PersistentData data = PersistentData.get(ctx.getSource().getLevel());
        Faction faction = data.getFactionByPlayer(player.getUUID());

        if (faction == null) {
            ctx.getSource().sendFailure(PrefixUtil.error("You are not in a faction!"));
            return 0;
        }

        if (!faction.getMembers().contains(target.getUUID())) {
            ctx.getSource().sendFailure(PrefixUtil.error("That player is not in your faction!"));
            return 0;
        }

        FactionRank playerRank = faction.getRanks().get(player.getUUID());
        FactionRank targetRank = faction.getRanks().get(target.getUUID());

        if (playerRank.ordinal() >= targetRank.ordinal()) {
            ctx.getSource().sendFailure(PrefixUtil.error("Your rank is not high enough to demote a player!"));
            return 0;
        }
        if (targetRank == FactionRank.PRIVATE) {
            ctx.getSource().sendFailure(PrefixUtil.error("This player is already the lowest rank!"));
            return 0;
        }

        if (player.getUUID() == faction.getOwner()) {
            ctx.getSource().sendFailure(PrefixUtil.error("You cannot demote this player!"));
            return 0;
        }

        faction.getRanks().put(target.getUUID(), FactionRank.PRIVATE);
        data.setDirty();

        ctx.getSource().sendSuccess(() -> PrefixUtil.success(target.getName().getString() + " has been demoted to Private!"), false);
        return 1;
    }
}