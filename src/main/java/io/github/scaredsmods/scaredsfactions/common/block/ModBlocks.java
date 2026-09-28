package io.github.scaredsmods.scaredsfactions.common.block;

import io.github.scaredsmods.scaredsfactions.common.FactionMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FactionMod.MOD_ID);

    public static final RegistryObject<Block> BEACON_SUPPORT_BLOCK = BLOCKS.register("beacon_support", () -> new Block(BlockBehaviour.Properties.of()));

    public static void register(IEventBus bus) {
        bus.register(BLOCKS);
    }
}
