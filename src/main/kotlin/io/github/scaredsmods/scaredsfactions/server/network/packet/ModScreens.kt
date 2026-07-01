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
package io.github.scaredsmods.scaredsfactions.server.network.packet

import com.mojang.authlib.GameProfile
import io.github.scaredsmods.scaredsfactions.client.screen.menu.ConfirmTransferOwnershipMenu
import io.github.scaredsmods.scaredsfactions.client.screen.menu.FactionSettingsMenu
import io.github.scaredsmods.scaredsfactions.client.screen.menu.ManageFactionMenu
import io.github.scaredsmods.scaredsfactions.client.screen.menu.RenameFactionMenu
import io.github.scaredsmods.scaredsfactions.client.screen.menu.TransferOwnershipMenu
import io.github.scaredsmods.scaredsfactions.client.screen.menu.ViewMembersMenu
import io.github.scaredsmods.scaredsfactions.faction.Faction
import io.github.scaredsmods.scaredsfactions.faction.setting.BooleanFactionSetting
import io.netty.buffer.ByteBuf
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu



enum class ModScreens {
	MANAGE_FACTION {
		override fun createMenu(id: Int, inv: Inventory?, player: ServerPlayer): AbstractContainerMenu {
			return ManageFactionMenu(id, inv)
		}
	},
	RENAME_FACTION {
		override fun createMenu(id: Int, inv: Inventory?, player: ServerPlayer): AbstractContainerMenu {
			return RenameFactionMenu(id, inv)
		}
	},
	TRANSFER_OWNERSHIP {

		override fun writeBuf(player: ServerPlayer, buf: FriendlyByteBuf) {
			val data = Faction.FactionSavedData.getSavedData(player.serverLevel())
			val faction = data.getFactionFromPlayer(player.uuid) ?: return
			val profileMembers = buildProfileMap(faction, player)
			buf.writeInt(profileMembers.size)
			profileMembers.forEach { (profile: GameProfile, rank: Faction.Rank) ->
				buf.writeGameProfile(profile)
				buf.writeEnum(rank)
			}
		}

		override fun createMenu(id: Int, inv: Inventory?, player: ServerPlayer): AbstractContainerMenu {
			val data = Faction.FactionSavedData.getSavedData(player.serverLevel())
			val faction = data.getFactionFromPlayer(player.uuid) ?: throw IllegalStateException("Player is not in a faction")
			val profileMembers = buildProfileMap(faction, player)
			return TransferOwnershipMenu(id, inv, profileMembers)
		}
	},
	VIEW_MEMBERS {

		override fun writeBuf(player: ServerPlayer, buf: FriendlyByteBuf) {
			val data = Faction.FactionSavedData.getSavedData(player.serverLevel())
			val faction = data.getFactionFromPlayer(player.uuid) ?: return
			val profileMembers = buildProfileMap(faction, player)
			buf.writeInt(profileMembers.size)
			profileMembers.forEach { (profile, rank) ->
				buf.writeGameProfile(profile)
				buf.writeEnum(rank)
			}
		}

		override fun createMenu(id: Int, inv: Inventory?, player: ServerPlayer): AbstractContainerMenu {
			val data = Faction.FactionSavedData.getSavedData(player.serverLevel())
			val faction = data.getFactionFromPlayer(player.uuid) ?: throw IllegalStateException("Player is not in a faction")
			val profileMembers = buildProfileMap(faction, player)
			return ViewMembersMenu(id, inv, profileMembers)
		}
	},
	FACTION_SETTINGS {

		override fun writeBuf(player: ServerPlayer, buf: FriendlyByteBuf) {
			val data = Faction.FactionSavedData.getSavedData(player.serverLevel())
			val faction = data.getFactionFromPlayer(player.uuid) ?: return
			for (setting in faction.settings) {
				if (setting is BooleanFactionSetting) {
					buf.writeBoolean(setting.get())
				}
			}
		}

		override fun createMenu(id: Int, inv: Inventory?, player: ServerPlayer): AbstractContainerMenu {
			val data = Faction.FactionSavedData.getSavedData(player.serverLevel())
			val faction = data.getFactionFromPlayer(player.uuid) ?: throw IllegalStateException("Player is not in a faction")
			return FactionSettingsMenu(id, inv, faction.settings)
		}
	},
	CONFIRM_TRANSFER {
		override fun createMenu(id: Int, inv: Inventory?, player: ServerPlayer): AbstractContainerMenu {
			val data = Faction.FactionSavedData.getSavedData(player.serverLevel())
			val faction = data.getFactionFromPlayer(player.uuid) ?: throw IllegalStateException("Player is not in a faction")
			return ConfirmTransferOwnershipMenu(id, inv, faction.pendingTransfer)
		}

		override fun writeBuf(player: ServerPlayer, buf: FriendlyByteBuf) {
			val data = Faction.FactionSavedData.getSavedData(player.serverLevel())
			val faction = data.getFactionFromPlayer(player.uuid) ?: return
			buf.writeUUID(faction.pendingTransfer)
		}
	},
	CLOSE {
		override fun createMenu(id: Int, inv: Inventory?, player: ServerPlayer): AbstractContainerMenu {
			throw NotImplementedError("Close does not open any screen!");
		}
	};


	fun getTitle(): Component {
		return Component.literal(name.replace("_", " "))
	}

	abstract fun createMenu(id: Int, inv: Inventory?, player: ServerPlayer): AbstractContainerMenu
	open fun writeBuf(player: ServerPlayer, buf: FriendlyByteBuf) {}


	companion object {
		fun buildProfileMap(faction: Faction, player: ServerPlayer): Map<GameProfile, Faction.Rank> {
			val result = mutableMapOf<GameProfile, Faction.Rank>()
			for ((uuid, rank) in faction.members) {
				val onlineMember = player.server.playerList.getPlayer(uuid)
				val profile = onlineMember?.gameProfile
					?: player.server.profileCache?.get(uuid)?.orElse(GameProfile(uuid, "Unknown"))
					?: GameProfile(uuid, "Unknown")
				result[profile] = rank
			}
			return result
		}
	}

}
