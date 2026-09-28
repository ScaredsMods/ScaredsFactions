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
package io.github.scaredsmods.scaredsfactions.common.faction;

import io.github.scaredsmods.scaredsfactions.common.ModConfigs;
import io.github.scaredsmods.scaredsfactions.common.ModTranslations;
import io.github.scaredsmods.scaredsfactions.common.config.LanguageOptions;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.AbstractFactionSetting;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.BooleanFactionSetting;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.EnumFactionSetting;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FactionSettings {

	public static List<AbstractFactionSetting<?, ?>> settings = new ArrayList<>();

	public static final BooleanFactionSetting INFO_VISIBLE = register(new BooleanFactionSetting(true, "isInfoVisible", translatableDisplayNameComponent(ModTranslations.INFO_VISIBLE_DISPLAY_NAME), translatableLoreComponent(ModTranslations.INFO_VISIBLE_LORE_LINE_1)));
	public static final BooleanFactionSetting VANILLA_FRIENDLY_FIRE = register(new BooleanFactionSetting(ModConfigs.commonConfig.factionSettingOverrides.doOverrideEnableVanillaFriendlyFire.get() && ModConfigs.commonConfig.factionSettingOverrides.overrideEnableVanillaFriendlyFire.get(),
			"enableVanillaFriendlyFire", translatableDisplayNameComponent(ModTranslations.VANILLA_FRIENDLY_FIRE_DISPLAY_NAME), translatableLoreComponent(ModTranslations.VANILLA_FRIENDLY_FIRE_LORE_LINE_1), translatableLoreComponent(ModTranslations.VANILLA_FRIENDLY_FIRE_LORE_LINE_2)));
	public static final BooleanFactionSetting TACZ_FRIENDLY_FIRE = register(new BooleanFactionSetting(ModConfigs.commonConfig.factionSettingOverrides.doOverrideEnableTACZFriendlyFire.get() && ModConfigs.commonConfig.factionSettingOverrides.overrideEnableTACZFriendlyFire.get(),
			"enableTACZFriendlyFire", translatableDisplayNameComponent(ModTranslations.TACZ_FRIENDLY_FIRE_DISPLAY_NAME), true, "tacz", translatableLoreComponent(ModTranslations.TACZ_FRIENDLY_FIRE_LORE_LINE_1), translatableLoreComponent(ModTranslations.TACZ_FRIENDLY_FIRE_LORE_LINE_2), translatableLoreComponent(ModTranslations.TACZ_FRIENDLY_FIRE_LORE_LINE_3)));
	public static final BooleanFactionSetting SBW_FRIENDLY_FIRE = register(new BooleanFactionSetting(ModConfigs.commonConfig.factionSettingOverrides.doOverrideEnableSBWFriendlyFire.get() && ModConfigs.commonConfig.factionSettingOverrides.overrideEnableSBWFriendlyFire.get(),
			"enableSBWFriendlyFire", translatableDisplayNameComponent(ModTranslations.SBW_FRIENDLY_FIRE_DISPLAY_NAME), true, "superbwarfare", translatableLoreComponent(ModTranslations.SBW_FRIENDLY_FIRE_LORE_LINE_1), translatableLoreComponent(ModTranslations.SBW_FRIENDLY_FIRE_LORE_LINE_2), translatableLoreComponent(ModTranslations.SBW_FRIENDLY_FIRE_LORE_LINE_3)));
	public static final EnumFactionSetting<Faction.Rank> OWNER_RANK = register(new EnumFactionSetting<>(
			ModConfigs.commonConfig.factionSettingOverrides.doOverrideDefaultOwnerRank.get() == true ? ModConfigs.commonConfig.factionSettingOverrides.overrideDefaultOwnerRank.get() == LanguageOptions.PREFER_STADHOUDER ? Faction.Rank.STADHOUDER : Faction.Rank.GENERALISSIMUS : Faction.Rank.GENERALISSIMUS,
			"ownerRank",
			translatableDisplayNameComponent(ModTranslations.OWNER_RANK_SETTING_DISPLAY_NAME),
			Faction.Rank.class,
			new Faction.Rank[] { Faction.Rank.GENERALISSIMUS,  Faction.Rank.STADHOUDER },
			translatableLoreComponent(ModTranslations.OWNER_RANK_SETTING_LORE_LINE_1), translatableLoreComponent(ModTranslations.OWNER_RANK_SETTING_LORE_LINE_2), translatableLoreComponent(ModTranslations.OWNER_RANK_SETTING_LORE_LINE_3)));

	public static final BooleanFactionSetting ENABLE_FRIENDLY_GLOWING = register(new BooleanFactionSetting(
			!ModConfigs.commonConfig.factionSettingOverrides.doOverrideGlowing.get() || ModConfigs.commonConfig.factionSettingOverrides.overrideFriendlyGlowing.get(), "enableFriendlyGlow", translatableDisplayNameComponent(ModTranslations.ENABLE_FRIENDLY_GLOWING_DISPLAY_NAME), translatableLoreComponent(ModTranslations.ENABLE_FRIENDLY_GLOWING_LORE_LINE_1)));
	public static final EnumFactionSetting<ChatFormatting> GLOW_COLOUR = register(new EnumFactionSetting<>(
			ModConfigs.commonConfig.factionSettingOverrides.doOverrideFriendlyColour.get() && ModConfigs.commonConfig.factionSettingOverrides.overrideFriendlyColour.get().isColor()
					? ModConfigs.commonConfig.factionSettingOverrides.overrideFriendlyColour.get()
					: ChatFormatting.GREEN,
			"glowColour",
			translatableDisplayNameComponent(ModTranslations.GLOW_COLOUR_DISPLAY_NAME),
			ChatFormatting.class,
			Arrays.stream(ChatFormatting.values()).filter(ChatFormatting::isColor).toArray(ChatFormatting[]::new),
			translatableLoreComponent(ModTranslations.GLOW_COLOUR_LORE_LINE_1)
	));

	public static <T, S extends AbstractFactionSetting<T, S>> S register(S setting) {
		settings.add(setting);
		return setting;
	}

	public static <V, T extends AbstractFactionSetting<V, T>> T getSettingByNbtId(String nbtId, Class<T> settingClass) {
		return settings.stream()
				.filter(s -> s.getNbtId().equals(nbtId) && settingClass.isInstance(s))
				.map(settingClass::cast)
				.findFirst()
				.orElse(null);
	}

	public static Component translatableLoreComponent(String translationKey) {
		return Component.translatable(translationKey).withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false));
	}

	public static Component translatableDisplayNameComponent(String translationKey) {
		return Component.translatable(translationKey).withStyle(style -> style.withColor(ChatFormatting.YELLOW).withItalic(false));
	}

	public static void init() {
	}
}
