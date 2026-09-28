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
package io.github.scaredsmods.scaredsfactions.common;


public class ModTranslations {

	// General
	public static final String NOT_A_PLAYER = FactionMod.translation("command.message.not_a_player");
	public static final String NO_FACTION = FactionMod.translation("command.message.no_faction");
	public static final String INSUFFICIENT_RANK = FactionMod.translation("command.message.rank.insufficient");
	public static final String PLAYER_NOT_IN_FACTION = FactionMod.translation("command.message.player_not_in_faction");

	// Command
	public static final String CONFIRM_TRANSFER = FactionMod.translation("command.screen.open.confirm_transfer");
	public static final String OWNER_KICK = FactionMod.translation("command.kick.message.owner");
	public static final String SELF_KICK = FactionMod.translation("command.kick.message.self");
	public static final String KICK_SUCCESS = FactionMod.translation("command.kick.message.success");
	public static final String PLAYER_NOT_IN_FACTION_PROMOTE = FactionMod.translation("command.promote.message.player_not_in_faction");
	public static final String EQUAL_RANK_PROMOTE = FactionMod.translation("command.promote.message.equal_or_higher_rank");
	public static final String PROMOTE_SUCCESS = FactionMod.translation("command.promote.message.success");
	public static final String NO_EXISTING_FACTIONS = FactionMod.translation("command.list.message.no_factions");
	public static final String NO_VISIBLE_FACTIONS = FactionMod.translation("command.list.message.no_visible_factions");
    public static final String FACTION_INFO_FACTION_NAME = FactionMod.translation("command.list.message.faction_info.faction_name");
    public static final String FACTION_INFO_FACTION_MEMBERS = FactionMod.translation("command.list.message.faction_info.faction_members");
	public static final String LEAVE_TRANSFER_OWNERSHIP = FactionMod.translation("command.leave.message.transfer_ownership");
	public static final String LEAVE_SUCCESS = FactionMod.translation("command.leave.message.success");
	public static final String NO_BEACON = FactionMod.translation("command.home.message.no_beacon");
	public static final String HOME_COOLDOWN = FactionMod.translation("command.home.message.on_cooldown");
	public static final String HOME_SUCCESS = FactionMod.translation("command.home.message.success");
	public static final String PRIVATE_FACTION = FactionMod.translation("command.info.message.private_faction");
	public static final String MAX_MEMBERS = FactionMod.translation("command.invite.message.max_members_hit");
	public static final String TARGET_PLAYER_IN_FACTION = FactionMod.translation("command.invite.message.target_player_in_faction");
	public static final String INVITED_PLAYER_MESSAGE = FactionMod.translation("command.invite.message.invited_player.pending_invite");
	public static final String INVITING_PLAYER_MESSAGE = FactionMod.translation("command.invite.message.inviting_player.sent_invite");
	public static final String TARGETED_FACTION_DOESNT_EXIST = FactionMod.translation("command.disband.message.targeted_faction_doesnt_exist");
	public static final String FACTION_DISBANDED_BY_ADMIN = FactionMod.translation("command.disband.message.disband_by_admin");
	public static final String FACTION_DISBAND_SUCCESS_ADMIN = FactionMod.translation("command.disband.message.success_admin");
	public static final String FACTION_OWNER_CAN_DISBAND = FactionMod.translation("command.disband.message.only_disbanded_by_owner");
	public static final String FACTION_DISBANDED_MEMBER = FactionMod.translation("command.disband.message.disband_member");
	public static final String FACTION_DISBAND_SUCCESS = FactionMod.translation("command.disband.message.success");
	public static final String PLAYER_AT_LOWEST_RANK = FactionMod.translation("command.demote.message.player_cannot_be_demoted");
	public static final String DEMOTE_SUCCESS = FactionMod.translation("command.demote.message.success");
	public static final String NAME_TAKEN = FactionMod.translation("command.create.message.name_taken");
	public static final String CREATE_FACTION_SUCCESS = FactionMod.translation("command.create.success");
	public static final String PLAYER_IN_FACTION = FactionMod.translation("command.invite.accept.message.player_in_faction");
	public static final String FACTION_NOT_EXISTING = FactionMod.translation("command.invite.accept.message.faction_not_existing");
	public static final String NO_INVITES = FactionMod.translation("command.invite.accept.no_invites");
	public static final String ACCEPT_SUCCESS = FactionMod.translation("command.invite.accept.message.success");
	public static final String ACCEPT_SUCCESS_FACTION_OWNER =  FactionMod.translation("command.invite.accept.message.success_owner");
	public static final String HELP_COMMAND_CREATE = FactionMod.translation("command.help.command.create.desc");
	public static final String HELP_COMMAND_DEMOTE = FactionMod.translation("command.help.command.demote.desc");
	public static final String HELP_COMMAND_DISBAND_BY_ADMIN = FactionMod.translation("command.help.command.disband_admin.desc");
	public static final String HELP_COMMAND_DISBAND = FactionMod.translation("command.help.command.disband.desc");
	public static final String HELP_COMMAND_HELP = FactionMod.translation("command.help.command.help.desc");
	public static final String HELP_COMMAND_HOME = FactionMod.translation("command.help.command.home.desc");
	public static final String HELP_COMMAND_INFO = FactionMod.translation("command.help.command.info.desc");
	public static final String HELP_COMMAND_INVITE = FactionMod.translation("command.help.command.invite.desc");
	public static final String HELP_COMMAND_INVITE_ACCEPT = FactionMod.translation("command.help.command.invite.accept.desc");
	public static final String HELP_COMMAND_KICK = FactionMod.translation("command.help.command.kick.desc");
	public static final String HELP_COMMAND_LEAVE = FactionMod.translation("command.help.command.leave.desc");
	public static final String HELP_COMMAND_LIST = FactionMod.translation("command.help.command.list.desc");
	public static final String HELP_COMMAND_MANAGE = FactionMod.translation("command.help.command.manage.desc");
	public static final String HELP_COMMAND_PROMOTE = FactionMod.translation("command.help.command.promote.desc");

	// Faction Settings
	public static final String BOOLEAN_SETTING_VALUE_AS_COMPONENT_ENABLED = FactionMod.translation("api.faction_setting.boolean.enabled");
	public static final String BOOLEAN_SETTING_VALUE_AS_COMPONENT_DISABLED = FactionMod.translation("api.faction_setting.boolean.disabled");

	public static final String INFO_VISIBLE_DISPLAY_NAME = FactionMod.translation("faction.setting.info_visible.display_name");
	public static final String INFO_VISIBLE_LORE_LINE_1 = FactionMod.translation("faction.setting.info_visible.lore.line1");

	public static final String VANILLA_FRIENDLY_FIRE_DISPLAY_NAME = FactionMod.translation("faction.setting.vanilla_friendly_fire.display_name");
	public static final String VANILLA_FRIENDLY_FIRE_LORE_LINE_1 = FactionMod.translation("faction.setting.vanilla_friendly_fire.lore.line1");
	public static final String VANILLA_FRIENDLY_FIRE_LORE_LINE_2 = FactionMod.translation("faction.setting.vanilla_friendly_fire.lore.line2");

	public static final String TACZ_FRIENDLY_FIRE_DISPLAY_NAME = FactionMod.translation("faction.setting.tacz_friendly_fire.display_name");
	public static final String TACZ_FRIENDLY_FIRE_LORE_LINE_1 = FactionMod.translation("faction.setting.tacz_friendly_fire.lore.line1");
	public static final String TACZ_FRIENDLY_FIRE_LORE_LINE_2 = FactionMod.translation("faction.setting.tacz_friendly_fire.lore.line2");
	public static final String TACZ_FRIENDLY_FIRE_LORE_LINE_3 = FactionMod.translation("faction.setting.tacz_friendly_fire.lore.line3");

	public static final String SBW_FRIENDLY_FIRE_DISPLAY_NAME = FactionMod.translation("faction.setting.sbw_friendly_fire.display_name");
	public static final String SBW_FRIENDLY_FIRE_LORE_LINE_1 = FactionMod.translation("faction.setting.sbw_friendly_fire.lore.line1");
	public static final String SBW_FRIENDLY_FIRE_LORE_LINE_2 = FactionMod.translation("faction.setting.sbw_friendly_fire.lore.line2");
	public static final String SBW_FRIENDLY_FIRE_LORE_LINE_3 = FactionMod.translation("faction.setting.sbw_friendly_fire.lore.line3");

	public static final String OWNER_RANK_SETTING_DISPLAY_NAME = FactionMod.translation("faction.setting.owner_rank.display_name");
	public static final String OWNER_RANK_SETTING_LORE_LINE_1 = FactionMod.translation("faction.setting.owner_rank.lore.line1");
	public static final String OWNER_RANK_SETTING_LORE_LINE_2 = FactionMod.translation("faction.setting.owner_rank.lore.line2");
	public static final String OWNER_RANK_SETTING_LORE_LINE_3 = FactionMod.translation("faction.setting.owner_rank.lore.line3");

	public static final String ENABLE_FRIENDLY_GLOWING_DISPLAY_NAME = FactionMod.translation("faction.setting.enable_friendly_glowing.display_name");
	public static final String ENABLE_FRIENDLY_GLOWING_LORE_LINE_1 = FactionMod.translation("faction.setting.enable_friendly_glowing.lore.line1");

	public static final String GLOW_COLOUR_DISPLAY_NAME = FactionMod.translation("faction.setting.glow_colour.display_name");
	public static final String GLOW_COLOUR_LORE_LINE_1 = FactionMod.translation("faction.setting.glow_color.lore.line1");

	// Menus
	public static final String CURRENT_VALUE = FactionMod.translation("menu.faction_setting.item.lore.current_value");
	public static final String BOOLEAN_SETTING_TOGGLE = FactionMod.translation("menu.faction_setting.item.lore.toggle");
	public static final String NUMERIC_SETTING_INCREMENT = FactionMod.translation("menu.faction_setting.item.lore.increment");
	public static final String NUMERIC_SETTING_DECREMENT = FactionMod.translation("menu.faction_setting.item.lore.decrement");
	public static final String STRING_SETTING_EDIT = FactionMod.translation("menu.faction_setting.item.lore.edit");
	public static final String BACK_ITEM_NAME = FactionMod.translation("menu.faction_setting.item.back.name");

	public static final String MANAGE_MENU_ITEM_RENAME = FactionMod.translation("menu.manage.item.rename");
	public static final String MANAGE_MENU_ITEM_TRANSFER_OWNERSHIP = FactionMod.translation("menu.manage.item.transfer_ownership");
	public static final String MANAGE_MENU_ITEM_VIEW_MEMBERS = FactionMod.translation("menu.manage.item.view_members");
	public static final String MANAGE_MENU_ITEM_FACTION_SETTINGS = FactionMod.translation("menu.manage.item.faction_settings");
	public static final String MANAGE_MENU_ITEM_RESET_BEACON_POS = FactionMod.translation("menu.manage.item.reset_beacon_pos");
	public static final String MANAGE_MENU_CLOSE = FactionMod.translation("menu.manage.item.close");

	public static final String TRANSFER_OWNERSHIP_MENU_PLAYER_ITEM_LORE_LINE_1 = FactionMod.translation("menu.transfer_ownership.item.player.lore.line1");
	public static final String TRANSFER_OWNERSHIP_MENU_PLAYER_ITEM_LORE_LINE_2 = FactionMod.translation("menu.transfer_ownership.item.player.lore.line2");

	public static final String VIEW_MEMBERS_MENU_PLAYER_ITEM_LORE_LINE_1 = FactionMod.translation("menu.view_members.item.player.lore.line1");
	public static final String VIEW_MEMBERS_MENU_PLAYER_ITEM_LORE_LINE_2 = FactionMod.translation("menu.view_members.item.player.lore.line2");
	public static final String VIEW_MEMBERS_MENU_PLAYER_ITEM_LORE_LINE_3 = FactionMod.translation("menu.view_members.item.player.lore.line3");

	// Screens
	public static final String BUTTON_CONFIRM = FactionMod.translation("screen.button.confirm");
	public static final String BUTTON_BACK = FactionMod.translation("screen.button.back");

	// Menu & Screen
	public static final String MANAGE_FACTION_LABEL = FactionMod.translation("menu_screen.manage_faction.label");
	public static final String EDIT_STRING_SETTING_LABEL = FactionMod.translation("menu_screen.edit_string_setting.label");
	public static final String RENAME_FACTION_LABEL = FactionMod.translation("menu_screen.rename_faction.label");
	public static final String FACTION_SETTINGS_LABEL = FactionMod.translation("menu_screen.faction_settings.label");
	public static final String TRANSFER_OWNERSHIP_LABEL = FactionMod.translation("menu_screen.transfer_ownership.label");
	public static final String VIEW_MEMBERS_LABEL = FactionMod.translation("menu_screen.view_members.label");
	public static final String RESET_BEACON_POS_LABEL = FactionMod.translation("menu_screen.reset_beacon_pos.label");
	public static final String CONFIRM_TRANSFER_OWNERSHIP_LABEL = FactionMod.translation("menu_screen.transfer_ownership.confirm.label");

	// Packet
	public static final String RESET_BEACON_POS_PACKET_MESSAGE = FactionMod.translation("packet.reset_beacon_pos.no_beacon_message");

	// Items
	public static final String RESPAWN_BEACON_NAME = FactionMod.translation("item.respawn_beacon.name");
	public static final String RESPAWN_BEACON_LORE_LINE_1 = FactionMod.translation("item.respawn_beacon.lore.line1");
	public static final String RESPAWN_BEACON_LORE_LINE_2 = FactionMod.translation("item.respawn_beacon.lore.line2");
	public static final String RESPAWN_BEACON_LORE_LINE_3 = FactionMod.translation("item.respawn_beacon.lore.line3");

	// Events
	public static final String BEACON_DESTROYED_ON_RESPAWN = FactionMod.translation("event.on_respawn.beacon_destroyed");
	public static final String BEACON_DESTROYED_ON_JOIN = FactionMod.translation("event.on_join.beacon_destroyed");
	public static final String BEACON_NOT_IN_MAIN_HAND_ON_PLACE = FactionMod.translation("event.on_place.message.beacon_not_in_main_hand");
	public static final String BEACON_NOT_IN_OVERWORLD_ON_PLACE = FactionMod.translation("event.on_place.message.not_in_overworld");
	public static final String PLAYER_NOT_IN_SURVIVAL_ON_PLACE = FactionMod.translation("event.on_place.not_in_survival");
	public static final String FACTION_HAS_BEACON_ON_PLACE = FactionMod.translation("event.on_place.faction_has_beacon");
	public static final String PLACE_SUCCESS = FactionMod.translation("event.on_place.success");
	public static final String PLAYER_NOT_IN_FACTION_ON_BREAK = FactionMod.translation("event.on_break.not_in_faction");
	public static final String SAME_FACTION_ON_BREAK = FactionMod.translation("event.on_break.same_faction");
	public static final String NO_FACTION_MEMBERS_ONLINE_ON_BREAK = FactionMod.translation("event.on_break.no_members_online");
	public static final String BREAK_SUCCESS_MEMBER = FactionMod.translation("event.on_break.success_member");
	public static final String BREAK_SUCCESS = FactionMod.translation("event.on_break.success");
	public static final String BREAK_SUCCESS_BROADCAST = FactionMod.translation("event.on_break.success_broadcast");
    public static final String BEACON_NOT_VISIBLE = FactionMod.translation("event.on_place.beacon_not_visible");

}
