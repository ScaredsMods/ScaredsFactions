/*
*  Copyright (C) 2026 ScaredRabbitNL
*
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with this program. If not, see <https://www.gnu.org/licenses/>.
*/
package io.github.scaredsmods.scaredsfactions.common.config;

import io.github.scaredsmods.scaredsfactions.common.FactionMod;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData;
import kotlin.ranges.IntRange;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.api.FileType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.event.api.ServerUpdateContext;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedEnum;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedLong;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

@Version(version = 1)
public class ModCommonConfig extends Config {

	public ModCommonConfig() {
		super(FactionMod.id("common"));
	}

	@Comment("Whether the /faction home command has a cooldown.")
	public ValidatedBoolean enableHomeCommandCooldown = new ValidatedBoolean(true);

	@Comment("The cooldown in seconds for the /faction home command if the enableHomeCommandCooldown setting above is set to true.")
	public ValidatedLong homeCommandCooldown =
			ValidatedNumber.withIncrement(new ValidatedLong(10800, 86400, 3600, ValidatedNumber.WidgetType.TEXTBOX_WITH_BUTTONS), 900L);

	@Comment("Whether the player respawns at their faction's beacon.")
	public ValidatedBoolean respawnPlayerAtFactionBeacon = new ValidatedBoolean(true);

	@Comment("Determines whether a team must have at least one player online for their beacon to be destroyed.")
	public ValidatedBoolean lastManOnline = new ValidatedBoolean(true);

	@Comment("The maximum amount of members a faction is allowed to have.")
	public ValidatedInt maxMembers = new ValidatedInt(Integer.MAX_VALUE, new IntRange(Integer.MIN_VALUE, Integer.MAX_VALUE), ValidatedNumber.WidgetType.TEXTBOX_WITH_BUTTONS);

	@Comment("Determines what happens to a player head when a player dies.")
	public ValidatedEnum<PlayerHeadOptions> playerHeadOptions = new ValidatedEnum<>(PlayerHeadOptions.ADD_TO_ATTACKER);

    @Comment("Whether a respawn beacon needs air access.")
    public ValidatedBoolean needAirForBeacon = new ValidatedBoolean(true);

	@Comment("Overrides for faction settings.")
	public FactionSettingOverrides factionSettingOverrides = new FactionSettingOverrides();


	public static class FactionSettingOverrides extends ConfigSection {
		public FactionSettingOverrides() {
			super();
		}

		@Comment("Whether the faction setting isInfoVisible should be overridden or not.")
		public ValidatedBoolean doOverrideInfoVisible = new ValidatedBoolean(false);

		@Comment("If this faction setting should be overridden, should the info of a faction appear when /faction info?")
		public ValidatedBoolean overrideInfoVisible = new ValidatedBoolean(false);

		public ValidatedBoolean doOverrideEnableTACZFriendlyFire = new ValidatedBoolean(false);

		@Comment("Setting this value to true will enable pvp within factions when using weapons from Timeless and Classics: Zero.")
		public ValidatedBoolean overrideEnableTACZFriendlyFire = new ValidatedBoolean(false);

		public ValidatedBoolean doOverrideEnableSBWFriendlyFire = new ValidatedBoolean(false);

		@Comment("Setting this value to true will enable pvp within factions when using weapons from Superb Warfare")
		public ValidatedBoolean overrideEnableSBWFriendlyFire = new ValidatedBoolean(false);

		public ValidatedBoolean doOverrideEnableVanillaFriendlyFire = new ValidatedBoolean(false);

		@Comment("Setting this value to true will enable pvp within factions when using vanilla weapons (axe and sword).")
		public ValidatedBoolean overrideEnableVanillaFriendlyFire = new ValidatedBoolean(false);

		@Comment("Whether the default owner rank a faction provides should be overridden.")
		public ValidatedBoolean doOverrideDefaultOwnerRank = new ValidatedBoolean(false);

		@Comment("If a faction's default rank should be overridden, which should be chosen? Stadhouder (Highest Dutch army commander in the 16th century) or Generalissimus (Latin for the Italian Generalissimo, formerly used by France, Italy, the USSR and more)?")
		public ValidatedEnum<LanguageOptions> overrideDefaultOwnerRank = new ValidatedEnum<>(LanguageOptions.class);

		@Comment("Whether the glowing setting of a faction should be overridden")
		public ValidatedBoolean doOverrideGlowing = new ValidatedBoolean(false);

		@Comment("If doOverrideGlowing is set to true, should friendly player glowing be enabled or not?")
		public ValidatedBoolean overrideFriendlyGlowing = new ValidatedBoolean(false);

		@Comment("Whether the default colour for friendly players should be overridden or not.")
		public ValidatedBoolean doOverrideFriendlyColour = new ValidatedBoolean(false);

		@Comment("If a faction's glowing colour should be overridden, what should the color be? Only actual colours (e.g. RED, AQUA, GREEN) take effect - formatting codes like BOLD or ITALIC aren't valid colours and will be ignored in favour of the default (GREEN).")
		public ValidatedEnum<ChatFormatting> overrideFriendlyColour = new ValidatedEnum<>(ChatFormatting.class);
	}

	@Override
	public @NotNull FileType fileType() {
		return FileType.TOML;
	}

	@Override
	public void onUpdateServer(@NotNull ServerUpdateContext context) {
		super.onUpdateServer(context);

		var server = context.getServer();
		FactionSavedData data = FactionSavedData.getSavedData(server.overworld());

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			Faction faction = data.getFactionFromPlayer(player.getUUID());
			if (faction == null) continue;
			if (!faction.hasBeacon()) continue;

			if (respawnPlayerAtFactionBeacon.get()) {
				player.setRespawnPosition(Level.OVERWORLD, faction.getBeaconPos().above(), 0.0F, true, true);
			} else {
				player.setRespawnPosition(Level.OVERWORLD, null, 0.0F, false, true);
			}
		}
	}
}
