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
package io.github.scaredsmods.scaredsfactions.common.datagen.lang.en;

import io.github.scaredsmods.scaredsfactions.common.FactionMod;
import io.github.scaredsmods.scaredsfactions.common.ModTranslations;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class EnUsLangProvider extends LanguageProvider {

	public EnUsLangProvider(PackOutput output) {
		super(output, FactionMod.MOD_ID, "en_us");
	}

	@Override
	public void addTranslations() {
		add(ModTranslations.NOT_A_PLAYER, "Only players can run this command!");
		add(ModTranslations.NO_FACTION, "You must be in a faction to run this command!");
		add(ModTranslations.INSUFFICIENT_RANK, "Your must be a higher rank to do this!");
		add(ModTranslations.PLAYER_NOT_IN_FACTION, "This player must be in your faction to do this!");
		add(ModTranslations.TARGET_PLAYER_IN_FACTION, "Target player is already in a faction! They must be in your faction to do this!");
		add(ModTranslations.CONFIRM_TRANSFER, "Confirm transfer?");
		add(ModTranslations.OWNER_KICK, "Only the leader of the faction can kick players from the faction!");
		add(ModTranslations.SELF_KICK, "Cannot kick yourself!");
		add(ModTranslations.KICK_SUCCESS, "Successfully kicked %s from your faction!");
		add(ModTranslations.PLAYER_NOT_IN_FACTION_PROMOTE, "Can't promote a player that isn't under your command!");
		add(ModTranslations.EQUAL_RANK_PROMOTE, "You cannot manage players of equal or higher rank!");
		add(ModTranslations.PROMOTE_SUCCESS, "You successfully promoted §7%s §ato %s!");
		add(ModTranslations.NO_EXISTING_FACTIONS, "No factions have been created yet!");
		add(ModTranslations.NO_VISIBLE_FACTIONS, "All factions are private!");
		add(ModTranslations.LEAVE_TRANSFER_OWNERSHIP, "You are the owner of this faction! Disband the faction or transfer ownership first!");
		add(ModTranslations.LEAVE_SUCCESS, "You left %s §a!");
		add(ModTranslations.NO_BEACON, "You cannot teleport to your beacon because it hasn't been placed down yet! Contact your faction leader!");
		add(ModTranslations.HOME_COOLDOWN, "You must wait another %s:%s:%s before using this command again!");
		add(ModTranslations.HOME_SUCCESS, "Successfully teleported to your faction's beacon!");
		add(ModTranslations.PRIVATE_FACTION, "This faction wants to be private!");
		add(ModTranslations.MAX_MEMBERS, "This faction has hit their member limit. Wait until the faction has room for more members!");
		add(ModTranslations.INVITED_PLAYER_MESSAGE, "You have been invited to join %s! Use /faction invite accept %s to join the faction!");
		add(ModTranslations.INVITING_PLAYER_MESSAGE, "Successfully invited %s to join the faction!");
		add(ModTranslations.TARGETED_FACTION_DOESNT_EXIST, "Target faction does not exist!");
		add(ModTranslations.FACTION_DISBANDED_BY_ADMIN, "Your faction has been disbanded by a staff member!");
		add(ModTranslations.FACTION_DISBAND_SUCCESS_ADMIN, "You successfully disbanded %s§a!");
		add(ModTranslations.FACTION_OWNER_CAN_DISBAND, "Only the owner can disband the faction!");
		add(ModTranslations.FACTION_DISBANDED_MEMBER, "Your faction has disbanded! Look for a new faction!");
		add(ModTranslations.FACTION_DISBAND_SUCCESS, "You successfully disbanded your faction!");
		add(ModTranslations.PLAYER_AT_LOWEST_RANK, "Target is already at the lowest rank (Pvt.). They cannot be demoted further!");
		add(ModTranslations.DEMOTE_SUCCESS, "Successfully demoted %s to %s!");
		add(ModTranslations.NAME_TAKEN, "This name has been taken! Please choose a new name!");
		add(ModTranslations.CREATE_FACTION_SUCCESS, "Faction %s created successfully!");
		add(ModTranslations.PLAYER_IN_FACTION, "To accept this invite, you must leave your current faction first!");
		add(ModTranslations.FACTION_NOT_EXISTING, "The faction you are trying to join doesn't exist!");
		add(ModTranslations.NO_INVITES, "You currently have no invites! Are you sure you were invited?");
		add(ModTranslations.ACCEPT_SUCCESS, "Successfully joined %s!");
		add(ModTranslations.ACCEPT_SUCCESS_FACTION_OWNER, "§f%s §ahas joined your faction");
		add(ModTranslations.HELP_COMMAND_CREATE, "Creates a faction with given name. Color codes supported!");
		add(ModTranslations.HELP_COMMAND_DEMOTE, "Demotes a player one rank.");
		add(ModTranslations.HELP_COMMAND_DISBAND_BY_ADMIN, "Forcefully disbands a faction. Only usable by ops or people with a certain permission!");
		add(ModTranslations.HELP_COMMAND_DISBAND, "Disbands the executors faction.");
		add(ModTranslations.HELP_COMMAND_HELP, "Shows this message.");
		add(ModTranslations.HELP_COMMAND_HOME, "If placed down, teleports the player to their respawn beacon.");
		add(ModTranslations.HELP_COMMAND_INFO, "Shows some information about the asked faction.");
		add(ModTranslations.HELP_COMMAND_INVITE, "Invites the selected player to your faction.");
		add(ModTranslations.HELP_COMMAND_INVITE_ACCEPT, "Accepts selected invite. The executor joins that faction.");
		add(ModTranslations.HELP_COMMAND_KICK, "Kicks selected player from the faction.");
		add(ModTranslations.HELP_COMMAND_LEAVE, "Leaves your current faction. Beware: That faction's members might be coming after you.");
		add(ModTranslations.HELP_COMMAND_LIST, "Lists all factions that exist, if there are any. Shows none if all factions have their info setting set to false!");
		add(ModTranslations.HELP_COMMAND_MANAGE, "Opens a menu where you can manage various aspects of your faction.");
		add(ModTranslations.HELP_COMMAND_PROMOTE, "Promotes a player to their next rank!");
		add(ModTranslations.BOOLEAN_SETTING_VALUE_AS_COMPONENT_ENABLED, "Enabled");
		add(ModTranslations.BOOLEAN_SETTING_VALUE_AS_COMPONENT_DISABLED, "Disabled");
		add(ModTranslations.INFO_VISIBLE_DISPLAY_NAME, "Display Faction Info");
		add(ModTranslations.INFO_VISIBLE_LORE_LINE_1, "When set to true, this faction is discoverable with /faction list and /faction info <name>.");
		add(ModTranslations.VANILLA_FRIENDLY_FIRE_DISPLAY_NAME, "Enable Vanilla Friendly Fire");
		add(ModTranslations.VANILLA_FRIENDLY_FIRE_LORE_LINE_1, "When set to true, vanilla pvp (axes, swords, etc) within this faction will be enabled.");
		add(ModTranslations.VANILLA_FRIENDLY_FIRE_LORE_LINE_2, "This setting is dependent on the global settings, thus can be overridden, thus this value is useless if overridden. Contact your server admin if you suspect the value isn't changing before submitting an issue!");
		add(ModTranslations.TACZ_FRIENDLY_FIRE_DISPLAY_NAME, "Enable Timeless and Classics: Zero Friendly Fire");
		add(ModTranslations.TACZ_FRIENDLY_FIRE_LORE_LINE_1, "When set to true, pvp with guns from Timeless and Classics: Zero will be enabled.");
		add(ModTranslations.TACZ_FRIENDLY_FIRE_LORE_LINE_2, "This setting does not appear when the mod Timeless and Classics: Zero is not installed.");
		add(ModTranslations.TACZ_FRIENDLY_FIRE_LORE_LINE_3, "This setting is dependent on the global settings, thus can be overridden, thus this value is useless if overridden. Contact your server admin if you suspect the value isn't changing before submitting an issue!");
		add(ModTranslations.SBW_FRIENDLY_FIRE_DISPLAY_NAME, "Enable Superbwarfare Friendly Fire");
		add(ModTranslations.SBW_FRIENDLY_FIRE_LORE_LINE_1, "When set to true, pvp with guns from Superbwarfare will be enabled.");
		add(ModTranslations.SBW_FRIENDLY_FIRE_LORE_LINE_2, "This setting does not appear when the mod Superbwarfare is not installed.");
		add(ModTranslations.SBW_FRIENDLY_FIRE_LORE_LINE_3, "This setting is dependent on the global settings, thus can be overridden, thus this value is useless if overridden. Contact your server admin if you suspect the value isn't changing before submitting an issue!");
		add(ModTranslations.OWNER_RANK_SETTING_DISPLAY_NAME, "Owner Rank");
		add(ModTranslations.OWNER_RANK_SETTING_LORE_LINE_1, "This setting determines which rank is the highest, Stadhouder or Generalissimus.");
		add(ModTranslations.OWNER_RANK_SETTING_LORE_LINE_2, "This setting is dependent on the global settings, thus can be overridden, thus this value is useless if overridden. Contact your server admin if you suspect the value isn't changing before submitting an issue!");
		add(ModTranslations.OWNER_RANK_SETTING_LORE_LINE_3, "It's default value is whatever the server owner has set in the config. It can be changed, or not!");
		add(ModTranslations.ENABLE_FRIENDLY_GLOWING_DISPLAY_NAME, "Enable Friendly Glowing");
		add(ModTranslations.ENABLE_FRIENDLY_GLOWING_LORE_LINE_1, "Enables a glowing effect for friendlies, with a color of your choosing!");
		add(ModTranslations.GLOW_COLOUR_DISPLAY_NAME, "Glow Color");
		add(ModTranslations.GLOW_COLOUR_LORE_LINE_1, "This setting determines which colour appears as an outline if Enable Friendly Player Glowing is enabled.");
		add(ModTranslations.CURRENT_VALUE, "Current value: ");
		add(ModTranslations.BOOLEAN_SETTING_TOGGLE, "Click to toggle setting!");
		add(ModTranslations.NUMERIC_SETTING_INCREMENT, "Left click to increment value!");
		add(ModTranslations.NUMERIC_SETTING_DECREMENT, "Right click to decrement value!");
		add(ModTranslations.STRING_SETTING_EDIT, "Click to edit value!");
		add(ModTranslations.BACK_ITEM_NAME, "Go back to main menu!");
		add(ModTranslations.MANAGE_FACTION_LABEL, "Manage Faction");
		add(ModTranslations.MANAGE_MENU_ITEM_RENAME, "Rename Faction");
		add(ModTranslations.MANAGE_MENU_ITEM_TRANSFER_OWNERSHIP, "Transfer Ownership");
		add(ModTranslations.MANAGE_MENU_ITEM_FACTION_SETTINGS, "View Faction Settings");
		add(ModTranslations.MANAGE_MENU_ITEM_VIEW_MEMBERS, "View Members");
		add(ModTranslations.MANAGE_MENU_ITEM_RESET_BEACON_POS, "Reset Beacon Position");
		add(ModTranslations.MANAGE_MENU_CLOSE, "Close");
		add(ModTranslations.TRANSFER_OWNERSHIP_LABEL, "Transfer Ownership");
		add(ModTranslations.TRANSFER_OWNERSHIP_MENU_PLAYER_ITEM_LORE_LINE_1, "Rank: ");
		add(ModTranslations.TRANSFER_OWNERSHIP_MENU_PLAYER_ITEM_LORE_LINE_2, "Choose this player as your successor?");
		add(ModTranslations.VIEW_MEMBERS_LABEL, "View Members");
		add(ModTranslations.VIEW_MEMBERS_MENU_PLAYER_ITEM_LORE_LINE_1, "Rank: ");
		add(ModTranslations.VIEW_MEMBERS_MENU_PLAYER_ITEM_LORE_LINE_2, "Left Click to promote this player");
		add(ModTranslations.VIEW_MEMBERS_MENU_PLAYER_ITEM_LORE_LINE_3, "Right Click to demote this player");
		add(ModTranslations.BUTTON_CONFIRM, "Confirm");
		add(ModTranslations.BUTTON_BACK, "Cancel");
		add(ModTranslations.EDIT_STRING_SETTING_LABEL, "Edit String Setting");
		add(ModTranslations.FACTION_SETTINGS_LABEL, "Faction Settings");
		add(ModTranslations.RESET_BEACON_POS_LABEL, "Reset Beacon Position");
		add(ModTranslations.CONFIRM_TRANSFER_OWNERSHIP_LABEL, "Confirm Ownership Transfer?");
		add(ModTranslations.RESET_BEACON_POS_PACKET_MESSAGE, "You must have a beacon to resets it's position!");
		add(ModTranslations.RESPAWN_BEACON_NAME, "Respawn Beacon");
		add(ModTranslations.RESPAWN_BEACON_LORE_LINE_1, "This is your faction's respawn beacon!");
		add(ModTranslations.RESPAWN_BEACON_LORE_LINE_2, "It functions as your bed, and lifeline!");
		add(ModTranslations.RESPAWN_BEACON_LORE_LINE_3, "Hide it well: If other factions manage to destroy it, you will no longer respawn!");
		add(ModTranslations.BEACON_DESTROYED_ON_RESPAWN, "Your faction's beacon was destroyed before you died! You will no longer respawn and have been put in spectator mode!");
		add(ModTranslations.BEACON_DESTROYED_ON_JOIN, "You were previously eliminated! Spectate your team!");
		add(ModTranslations.BEACON_NOT_IN_MAIN_HAND_ON_PLACE, "You must hold the beacon in your main hand to place it!");
		add(ModTranslations.BEACON_NOT_IN_OVERWORLD_ON_PLACE, "You can only place your beacon in the overworld!");
		add(ModTranslations.PLAYER_NOT_IN_SURVIVAL_ON_PLACE, "You must be in survival to place your beacon!");
		add(ModTranslations.FACTION_HAS_BEACON_ON_PLACE, "Your faction already has a beacon!");
		add(ModTranslations.PLACE_SUCCESS, "Your faction's respawn beacon has been placed!");
		add(ModTranslations.PLAYER_NOT_IN_FACTION_ON_BREAK, "You aren't in a faction! You are considered neutral and cannot break a faction's beacon!");
		add(ModTranslations.SAME_FACTION_ON_BREAK, "Use /faction manage to move your beacon!");
		add(ModTranslations.NO_FACTION_MEMBERS_ONLINE_ON_BREAK, "There must be at least one player of this faction online to break this faction's beacon. Currently, no one of this faction is online!");
		add(ModTranslations.BREAK_SUCCESS_MEMBER, "Your faction's respawn beacon was destroyed! You are on your last life!");
		add(ModTranslations.BREAK_SUCCESS, "You just destroyed %s's beacon. Kill them to knock them out!");
		add(ModTranslations.BREAK_SUCCESS_BROADCAST, "%s's beacon has been broken! Finish them!");
        add(ModTranslations.RENAME_FACTION_LABEL, "Rename Faction");
        add(ModTranslations.FACTION_INFO_FACTION_NAME, "Faction: %s");
        add(ModTranslations.FACTION_INFO_FACTION_MEMBERS, "Members: %s");
        add(ModTranslations.BEACON_NOT_VISIBLE, "Respawn beacon must be placed so it's findable!");
		add(ModTranslations.DEBUG_LIST_BEACON_POSITIONS_FACTION_NAME, "Faction: ");
		add(ModTranslations.DEBUG_LIST_BEACON_POSITIONS_POS_TEXT, "Beacon position: ");
		add(ModTranslations.DEBUG_LIST_BEACON_POSITION_NULL_ERROR, "Beacon has not been placed down yet!");

	}
}
