package io.github.scaredsmods.scaredsfactions.common.component;

import com.mojang.serialization.Codec;
import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class ModDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ScaredsFactionMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?> , DataComponentType<RespawnBeaconDataComponent>> RESPAWN_BEACON = register("respawn_beacon",
            builder -> builder.persistent(RespawnBeaconDataComponent.CODEC).networkSynchronized(RespawnBeaconDataComponent.STREAM_CODEC));

    private static <T>DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name,
                                                                                          UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENTS.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
