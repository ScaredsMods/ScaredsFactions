package io.github.scaredsmods.scaredsfactions.common.datagen;

import io.github.scaredsmods.scaredsfactions.common.FactionMod;
import io.github.scaredsmods.scaredsfactions.common.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {

    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, FactionMod.MOD_ID, existingFileHelper);
    }

    @Override
    public void addTags(HolderLookup.Provider pProvider) {
        tag(ModTags.Blocks.AIR)
                .add(Blocks.AIR)
                .add(Blocks.CAVE_AIR)
                .add(Blocks.VOID_AIR);
    }
}
