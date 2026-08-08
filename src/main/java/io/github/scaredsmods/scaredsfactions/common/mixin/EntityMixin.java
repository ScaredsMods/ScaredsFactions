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
package io.github.scaredsmods.scaredsfactions.common.mixin;


import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.BooleanFactionSetting;
import io.github.scaredsmods.scaredsfactions.common.faction.ClientFactionSavedData;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSettings;
import io.github.scaredsmods.scaredsfactions.common.util.FactionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Inject(method = "getTeamColor()I", at = @At("RETURN"), cancellable = true)
	public void getColor(CallbackInfoReturnable<Integer> cir) {
		Entity entity = (Entity) (Object) this;
		if (entity.getTeam() == null || entity.getTeam() != null) {
			if (entity instanceof Player player) {
				if (ClientFactionSavedData.isEqualFaction(player.getUUID())) {
					Faction faction = ClientFactionSavedData.getFactionFromPlayer(player.getUUID());
					if (faction != null) {
						boolean glowEnabled = faction.getSettingValue(FactionSettings.ENABLE_FRIENDLY_GLOWING.getNbtId(), BooleanFactionSetting.class);
						if (glowEnabled) {
							ChatFormatting color = faction.getSettingValue(FactionSettings.GLOW_COLOUR.getNbtId(), FactionUtil.<ChatFormatting>enumSetting());
							if (color != null) {
								cir.setReturnValue(color.getColor());
							}
						}
					}
				}
			}
		}
	}
}
