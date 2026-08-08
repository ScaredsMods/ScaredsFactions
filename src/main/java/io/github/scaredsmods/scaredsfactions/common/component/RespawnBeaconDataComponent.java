package io.github.scaredsmods.scaredsfactions.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record RespawnBeaconDataComponent(boolean isRespawnBeacon) {

    public static final Codec<RespawnBeaconDataComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("value1").forGetter(RespawnBeaconDataComponent::isRespawnBeacon)
            ).apply(instance, RespawnBeaconDataComponent::new)
    );
    public static final StreamCodec<ByteBuf, RespawnBeaconDataComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, RespawnBeaconDataComponent::isRespawnBeacon,
            RespawnBeaconDataComponent::new
    );

}
