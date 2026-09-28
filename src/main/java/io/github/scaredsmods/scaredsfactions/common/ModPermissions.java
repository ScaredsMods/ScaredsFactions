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

public class ModPermissions {

	public static final String ALL = FactionMod.permission("admin");

	public static final String CAN_USE_ROOT = FactionMod.permission("command.faction");
	public static final String CAN_CREATE_FACTION = FactionMod.permission("command.faction.create");
	public static final String CAN_DEMOTE_PLAYER = FactionMod.permission("command.faction.demote");
	public static final String CAN_DISBAND_FACTION =  FactionMod.permission("command.faction.disband");
	public static final String CAN_USE_DISBAND_BY_NAME = FactionMod.permission("command.faction.disband.name");
	public static final String CAN_USE_HELP = FactionMod.permission("command.faction.help");
	public static final String CAN_TELEPORT_TO_BEACON = FactionMod.permission("command.faction.home");
	public static final String CAN_GET_FACTION_INFO = FactionMod.permission("command.faction.info");
	public static final String CAN_INVITE_PLAYER_TO_FACTION = FactionMod.permission("command.faction.invite");
	public static final String CAN_KICK_PLAYER_FROM_FACTION = FactionMod.permission("command.faction.kick");
	public static final String CAN_LEAVE_FACTION = FactionMod.permission("command.faction.leave");
	public static final String CAN_LIST_FACTIONS = FactionMod.permission("command.faction.list");
	public static final String CAN_MANAGE_FACTION = FactionMod.permission("command.faction.manage");
	public static final String CAN_PROMOTE_PLAYER = FactionMod.permission("command.faction.promote");
	public static final String CAN_ACCEPT_INVITE =  FactionMod.permission("command.faction.invite.accept");
	public static final String CAN_DEBUG = FactionMod.permission("command.faction.debug");
}
