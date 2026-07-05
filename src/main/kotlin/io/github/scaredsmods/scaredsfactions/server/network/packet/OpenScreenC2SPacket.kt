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
import io.github.scaredsmods.scaredsfactions.api.server.network.packet.IAbstractFactionPacket
import io.github.scaredsmods.scaredsfactions.api.server.network.packet.PairPacket
import io.github.scaredsmods.scaredsfactions.client.screen.menu.*
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData
import io.github.scaredsmods.scaredsfactions.common.faction.Faction.Rank
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.inventory.MenuConstructor
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.NetworkHooks
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.collections.HashMap
import kotlin.collections.MutableMap

class OpenScreenC2SPacket(private val screen : ModScreens, private val title: Component) : PairPacket<ModScreens, Component, OpenScreenC2SPacket>(
	screen,
	title,
	{ buf, modScreens -> buf.writeEnum(modScreens)},
	{ buf, component -> buf.writeComponent(component)}
	) {

	companion object : IAbstractFactionPacket.Decoder<OpenScreenC2SPacket> {
		override fun decode(buf: FriendlyByteBuf): OpenScreenC2SPacket {
			return OpenScreenC2SPacket(buf.readEnum(ModScreens::class.java), buf.readComponent())
		}
	}

	override fun handle(packet: OpenScreenC2SPacket, ctx: Supplier<NetworkEvent.Context>) {
		ctx.get().enqueueWork {
			val player = ctx.get().getSender() ?: return@enqueueWork
			val data = FactionSavedData.getSavedData(player.serverLevel())
			when (screen) {
				ModScreens.MANAGE_FACTION -> NetworkHooks.openScreen(
					player, SimpleMenuProvider(
						MenuConstructor { id, inv, _ -> ManageFactionMenu(id, inv) },
						title
					)
				)
				ModScreens.RENAME_FACTION -> NetworkHooks.openScreen(
					player, SimpleMenuProvider(
						MenuConstructor { id, inv, _ -> RenameFactionMenu(id, inv) },
						Component.literal("Rename Faction")
					)
				)
				ModScreens.TRANSFER_OWNERSHIP -> {
					val faction = data.getFactionFromPlayer(player.uuid) ?: return@enqueueWork
					val profileMembers: MutableMap<GameProfile, Rank> = HashMap()
					for (entry in faction.members.entries) {
						val onlineMember = player.server.playerList.getPlayer(entry.key)
						val profile = onlineMember?.gameProfile
							?: player.server.profileCache?.get(entry.key)?.orElse(GameProfile(entry.key, "Unknown"))
							?: GameProfile(entry.key, "Unknown")
						profileMembers[profile] = entry.value
					}
					val filteredMembers = profileMembers.filter { (_, rank) -> rank == Rank.FIELD_MARSHAL }
					NetworkHooks.openScreen(player, SimpleMenuProvider(
						MenuConstructor { id, inv, _ -> TransferOwnershipMenu(id, inv, filteredMembers) },
						Component.literal("Transfer Ownership")
					), Consumer { buf ->
						buf!!.writeInt(filteredMembers.size)
						for (entry in filteredMembers.entries) {
							buf.writeGameProfile(entry.key)
							buf.writeEnum(entry.value)
						}
					})
				}
				ModScreens.VIEW_MEMBERS -> {
					val faction = data.getFactionFromPlayer(player.uuid) ?: return@enqueueWork
					val profileMembers: MutableMap<GameProfile, Rank> = LinkedHashMap()
					val sortedMembers = faction.members.entries
						.sortedByDescending { it.value.id }
					for (entry in sortedMembers) {
						val onlineMember = player.server.playerList.getPlayer(entry.key)
						val profile = onlineMember?.gameProfile
							?: player.server.profileCache?.get(entry.key)?.orElse(GameProfile(entry.key, "Unknown"))
							?: GameProfile(entry.key, "Unknown")
						profileMembers[profile] = entry.value
					}
					NetworkHooks.openScreen(player, SimpleMenuProvider(
						MenuConstructor { id, inv, _ -> ViewMembersMenu(id, inv, profileMembers) },
						Component.literal("View Members")
					), Consumer { buf ->
						buf!!.writeInt(profileMembers.size)
						for (entry in profileMembers.entries) {
							buf.writeGameProfile(entry.key)
							buf.writeEnum(entry.value)
						}
					})
				}
				ModScreens.FACTION_SETTINGS -> {
					val faction = data.getFactionFromPlayer(player.uuid) ?: return@enqueueWork
					NetworkHooks.openScreen(player, SimpleMenuProvider(
						MenuConstructor { id, inv, _ -> FactionSettingsMenu(id, inv, faction.settings) },
						Component.literal("Faction Settings")
					), Consumer { buf ->
						buf!!.writeVarInt(faction.settings.size)
						for (setting in faction.settings) {
							buf.writeUtf(setting.nbtId)
							val tag = CompoundTag()
							setting.save(tag)
							buf.writeNbt(tag)
						}
					})
				}
				ModScreens.CONFIRM_TRANSFER -> {
					val faction = data.getFactionFromPlayer(player.uuid) ?: return@enqueueWork
					val targetUUID = faction.pendingTransfer ?: return@enqueueWork
					NetworkHooks.openScreen(player, SimpleMenuProvider(
						{ id, inv, _ -> ConfirmTransferOwnershipMenu(id, inv, targetUUID) },
						title
					)
					) { buf -> buf.writeUUID(targetUUID) }
				}
				ModScreens.CLOSE -> player.closeContainer()
			}
		}
		ctx.get().packetHandled = true
	}


}
