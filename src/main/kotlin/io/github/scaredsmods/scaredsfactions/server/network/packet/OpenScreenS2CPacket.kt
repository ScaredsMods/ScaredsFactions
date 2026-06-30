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
import io.github.scaredsmods.scaredsfactions.client.screen.menu.*
import io.github.scaredsmods.scaredsfactions.faction.Faction.FactionSavedData
import io.github.scaredsmods.scaredsfactions.faction.Faction.Rank
import io.github.scaredsmods.scaredsfactions.faction.setting.BooleanFactionSetting
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.MenuConstructor
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.NetworkHooks
import java.util.Objects
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.collections.HashMap
import kotlin.collections.MutableMap

class OpenScreenS2CPacket(private val screen : ModScreens, private val title: Component) : AbstractFactionPacket<OpenScreenS2CPacket> {

	companion object : AbstractFactionPacket.Decoder<OpenScreenS2CPacket> {
		override fun decode(buf: FriendlyByteBuf): OpenScreenS2CPacket {
			return OpenScreenS2CPacket(buf.readEnum(ModScreens::class.java), buf.readComponent())
		}
	}


	override fun encode(packet: OpenScreenS2CPacket, buf: FriendlyByteBuf) {
		buf.writeEnum(packet.screen)
		buf.writeComponent(packet.title)
	}

	override fun handle(packet: OpenScreenS2CPacket, ctx: Supplier<NetworkEvent.Context>) {
		ctx.get().enqueueWork {
			val player = ctx.get().getSender() ?: return@enqueueWork
			val data = FactionSavedData.getSavedData(player.serverLevel())
			when (packet.screen) {
				ModScreens.MANAGE_FACTION -> NetworkHooks.openScreen(
					player, SimpleMenuProvider(
						MenuConstructor { id: Int, inv: Inventory?, p: Player? -> ManageFactionMenu(id, inv) },
						packet.title
					)
				)

				ModScreens.RENAME_FACTION -> NetworkHooks.openScreen(
					player, SimpleMenuProvider(
						MenuConstructor { id: Int, inv: Inventory?, p: Player? -> RenameFactionMenu(id, inv) },
						Component.literal("Rename Faction")
					)
				)

				ModScreens.TRANSFER_OWNERSHIP -> {
					val faction = data.getFactionFromPlayer(player.getUUID()) ?: return@enqueueWork

					val profileMembers: MutableMap<GameProfile, Rank> = HashMap()
					for (entry in faction.members.entries) {
						val onlineMember = Objects.requireNonNull(player.getServer())?.playerList
							?.getPlayer(entry.key)
						val profile = onlineMember!!.gameProfile
						Objects.requireNonNull(player.getServer()?.profileCache)
							?.get(entry.key)
							?.orElse(GameProfile(entry.key, "Unknown"))
						profileMembers[profile] = entry.value
					}

					val filteredMembers: Map<GameProfile, Rank> = profileMembers.filter { (_, rank) -> rank != Rank.GENERALISSIMUS && rank != Rank.STADHOUDER }
					NetworkHooks.openScreen(
						player, SimpleMenuProvider(
							MenuConstructor { id: Int, inv: Inventory?, p: Player? ->
								TransferOwnershipMenu(
									id,
									inv,
									filteredMembers
								)
							},
							Component.literal("Transfer Ownership")
						), Consumer { buf: FriendlyByteBuf? ->
							buf!!.writeInt(filteredMembers.size)
							for (entry in filteredMembers.entries) {
								buf.writeGameProfile(entry.key)
								buf.writeEnum(entry.value)
							}
						})
				}

				ModScreens.VIEW_MEMBERS -> {
					val faction = data.getFactionFromPlayer(player.getUUID()) ?: return@enqueueWork

					val profileMembers: MutableMap<GameProfile, Rank> = HashMap()
					for (entry in faction.members.entries) {
						val onlineMember = Objects.requireNonNull(player.getServer())?.playerList
							?.getPlayer(entry.key)
						val profile = onlineMember!!.gameProfile

						Objects.requireNonNull(player.getServer()!!.profileCache)
							?.get(entry.key)
							?.orElse(GameProfile(entry.key, "Unknown"))
						profileMembers[profile] = entry.value
					}

					NetworkHooks.openScreen(
						player, SimpleMenuProvider(
							MenuConstructor { id: Int, inv: Inventory?, p: Player? ->
								ViewMembersMenu(
									id,
									inv,
									profileMembers
								)
							},
							Component.literal("View Members")
						), Consumer { buf: FriendlyByteBuf? ->
							buf!!.writeInt(profileMembers.size)
							for (entry in profileMembers.entries) {
								buf.writeGameProfile(entry.key)
								buf.writeEnum(entry.value)
							}
						})
				}

				ModScreens.FACTION_SETTINGS -> {
					val faction = data.getFactionFromPlayer(player.getUUID()) ?: return@enqueueWork
					NetworkHooks.openScreen(
						player, SimpleMenuProvider(
							MenuConstructor { id: Int, inv: Inventory?, p: Player? ->
								FactionSettingsMenu(
									id,
									inv,
									faction.settings
								)
							},
							Component.literal("Faction Settings")
						), Consumer { buf: FriendlyByteBuf? ->
							for (setting in faction.settings) {
								if (setting is BooleanFactionSetting) {
									buf!!.writeBoolean(setting.get())
								}
							}
						})
				}

				ModScreens.CLOSE -> {
					player.closeContainer()
				}

				ModScreens.CONFIRM_TRANSFER -> {
					NetworkHooks.openScreen(player, SimpleMenuProvider({
						id: Int, inv: Inventory?, p: Player? ->
						ConfirmTransferOwnershipMenu(id, inv)
					}, Component.literal("Confirm Transfer?")))
				}
			}
		}
		ctx.get().packetHandled = true
	}


}
