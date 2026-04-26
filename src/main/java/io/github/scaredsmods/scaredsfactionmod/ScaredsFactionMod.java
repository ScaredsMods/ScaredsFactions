package io.github.scaredsmods.scaredsfactionmod;


import com.mojang.logging.LogUtils;
import io.github.scaredsmods.scaredsfactionmod.config.ModCommonConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;


@Mod(ScaredsFactionMod.MOD_ID)
public class ScaredsFactionMod
{
    public static final String MOD_ID = "factions";
    private static final Logger LOGGER = LogUtils.getLogger();


    public ScaredsFactionMod(FMLJavaModLoadingContext context)
    {
        ModConfigs.init();
    }
    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

}
