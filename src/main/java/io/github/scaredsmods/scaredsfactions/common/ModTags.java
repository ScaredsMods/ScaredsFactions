package io.github.scaredsmods.scaredsfactions.common;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static class Blocks {

        public static final TagKey<Block> AIR = create("air_blocks");

        private static TagKey<Block> create(String name) {
            return TagKey.create(Registries.BLOCK, FactionMod.id(name));
        }
    }
}
