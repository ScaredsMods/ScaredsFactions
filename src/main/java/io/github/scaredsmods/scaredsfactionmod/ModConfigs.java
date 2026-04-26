package io.github.scaredsmods.scaredsfactionmod;

import io.github.scaredsmods.scaredsfactionmod.config.ModCommonConfig;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;

public class ModConfigs {
    public static ModCommonConfig commonConfig = ConfigApiJava.registerAndLoadConfig(ModCommonConfig::new, RegisterType.BOTH);
    public static void init() {}
}
