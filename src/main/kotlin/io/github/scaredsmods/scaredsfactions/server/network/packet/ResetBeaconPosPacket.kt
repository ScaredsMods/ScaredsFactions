package io.github.scaredsmods.scaredsfactions.server.network.packet

import io.github.scaredsmods.scaredsfactions.api.server.network.packet.IAbstractFactionPacket
import io.github.scaredsmods.scaredsfactions.common.faction.Faction
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData
import io.github.scaredsmods.scaredsfactions.common.util.MessageUtil
import net.minecraft.ChatFormatting
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier
import java.util.function.UnaryOperator

class ResetBeaconPosPacket : IAbstractFactionPacket<ResetBeaconPosPacket> {
    override fun encode(packet: ResetBeaconPosPacket, buf: FriendlyByteBuf) {

    }

    companion object : IAbstractFactionPacket.Decoder<ResetBeaconPosPacket> {
        override fun decode(buf: FriendlyByteBuf): ResetBeaconPosPacket {
            return ResetBeaconPosPacket()
        }

    }

    override fun handle(packet: ResetBeaconPosPacket, ctx: Supplier<NetworkEvent.Context>) {
        ctx.get().enqueueWork {
            val player : ServerPlayer = ctx.get().sender ?: return@enqueueWork
            val data : FactionSavedData = FactionSavedData.getSavedData(player.serverLevel())
            val faction : Faction = data.getFactionFromPlayer(player.uuid) ?: return@enqueueWork
            if (!faction.hasBeacon()) {
                player.sendSystemMessage(MessageUtil.Prefix.error("You must have a beacon to do this!"))
                return@enqueueWork
            }
            player.serverLevel().destroyBlock(faction.beaconPos, false)
            faction.removeBeacon()

            val beacon = ItemStack(Items.BEACON)
            beacon.getOrCreateTag().putBoolean("respawn_beacon", true)
            beacon.setHoverName(
                Component.literal("Respawn Beacon")
                    .withStyle(UnaryOperator { style: Style? ->
                        style!!
                            .withBold(true)
                            .withColor(ChatFormatting.RED)
                            .withItalic(false)
                    })
            )
            val display = beacon.getOrCreateTagElement("display")
            val lore = ListTag()
            lore.add(
                StringTag.valueOf(
                    Component.Serializer.toJson(
                        Component.literal("This is your faction's respawn beacon!")
                            .withStyle(UnaryOperator { style: Style? ->
                                style!!
                                    .withColor(ChatFormatting.GRAY)
                                    .withItalic(false)
                            })
                    )
                )
            )
            lore.add(
                StringTag.valueOf(
                    Component.Serializer.toJson(
                        Component.literal("It functions as your bed, and lifeline!")
                            .withStyle(UnaryOperator { style: Style? ->
                                style!!
                                    .withColor(ChatFormatting.GRAY)
                                    .withItalic(false)
                            })
                    )
                )
            )

            lore.add(
                StringTag.valueOf(
                    Component.Serializer.toJson(
                        Component.literal("Hide it well: If other factions get a hold of it, you can no longer respawn!")
                            .withStyle(UnaryOperator { style: Style? ->
                                style!!
                                    .withBold(false)
                                    .withItalic(false)
                                    .withColor(ChatFormatting.GRAY)
                            })
                    )
                )
            )

            display.put("Lore", lore)
            player.inventory.add(beacon)
            data.save(player.serverLevel())
        }
        ctx.get().packetHandled = true
    }
}