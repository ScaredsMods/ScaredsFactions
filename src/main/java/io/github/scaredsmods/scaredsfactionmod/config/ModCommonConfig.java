package io.github.scaredsmods.scaredsfactionmod.config;

import io.github.scaredsmods.scaredsfactionmod.ScaredsFactionMod;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.api.FileType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;

public class ModCommonConfig extends Config {

    public ModCommonConfig() {
        super(ScaredsFactionMod.id("common"));
    }

    @Comment("Setting this value to true will enable pvp within factions when using weapons from Timeless and Classics : Zero.")
    public ValidatedBoolean enableTACZFriendlyFire = new ValidatedBoolean(false);

    @Comment("Setting this value to true will enable pvp within factions when using weapons from Superb Warfare")
    public ValidatedBoolean enableSBWFriendlyFire = new ValidatedBoolean(false);

    @Comment("Setting this value to true will enable pvp within factions when using vanilla weapons (axe and sword).")
    public ValidatedBoolean enableVanillaFriendlyFire = new ValidatedBoolean(false);

    @Comment("Whether the player respawns at their faction's beacon.")
    public ValidatedBoolean respawnPlayerAtFactionBeacon = new ValidatedBoolean(true);

    @Override
    public @NotNull FileType fileType() {
        return FileType.TOML;
    }

}
