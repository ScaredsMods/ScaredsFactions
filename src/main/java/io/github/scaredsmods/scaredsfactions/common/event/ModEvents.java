package io.github.scaredsmods.scaredsfactions.common.event;

import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.common.network.packet.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ScaredsFactionMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(DemotePlayerC2SPacket.TYPE, DemotePlayerC2SPacket.STREAM_CODEC, DemotePlayerC2SPacket::handle);
        registrar.playToServer(OpenEditStringSettingC2SPacket.TYPE, OpenEditStringSettingC2SPacket.STREAM_CODEC, OpenEditStringSettingC2SPacket::handle);
        registrar.playToServer(OpenScreenC2SPacket.TYPE, OpenScreenC2SPacket.STREAM_CODEC, OpenScreenC2SPacket::handle);
        registrar.playToServer(PendingOwnershipTransferC2SPacket.TYPE, PendingOwnershipTransferC2SPacket.STREAM_CODEC, PendingOwnershipTransferC2SPacket::handle);
        registrar.playToServer(PromotePlayerC2SPacket.TYPE, PromotePlayerC2SPacket.STREAM_CODEC, PromotePlayerC2SPacket::handle);
        registrar.playToServer(RenameFactionC2SPacket.TYPE, RenameFactionC2SPacket.STREAM_CODEC, RenameFactionC2SPacket::handle);
        registrar.playToServer(ResetBeaconPosC2SPacket.TYPE, ResetBeaconPosC2SPacket.STREAM_CODEC, ResetBeaconPosC2SPacket::handle);
        registrar.playToClient(SyncFactionDataS2CPacket.TYPE, SyncFactionDataS2CPacket.STREAM_CODEC, SyncFactionDataS2CPacket::handle);
        registrar.playToServer(TransferOwnershipC2SPacket.TYPE, TransferOwnershipC2SPacket.STREAM_CODEC, TransferOwnershipC2SPacket::handle);
        registrar.playToServer(UpdateFactionSettingC2SPacket.TYPE, UpdateFactionSettingC2SPacket.STREAM_CODEC, UpdateFactionSettingC2SPacket::handle);
    }
}
