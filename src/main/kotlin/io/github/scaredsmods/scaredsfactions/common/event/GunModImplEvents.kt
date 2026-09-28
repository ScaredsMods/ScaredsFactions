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
package io.github.scaredsmods.scaredsfactions.common.event;

import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.BooleanFactionSetting
import io.github.scaredsmods.scaredsfactions.common.ModConfigs
import io.github.scaredsmods.scaredsfactions.common.FactionMod
import io.github.scaredsmods.scaredsfactions.common.faction.Faction
import io.github.scaredsmods.scaredsfactions.common.faction.FactionSavedData
import io.github.scaredsmods.scaredsfactions.common.util.MessageUtil
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageType
import net.minecraftforge.event.entity.living.LivingHurtEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod
import com.atsuishio.superbwarfare.init.ModTags.DamageTypes as SuperbWarfareDamageTypes
import com.tacz.guns.init.ModDamageTypes as TaCZDamageTypes


@Mod.EventBusSubscriber(modid = FactionMod.MOD_ID)
object GunModImplEvents {

	@SubscribeEvent(priority = EventPriority.HIGH)
	fun disableInFactionFriendlyFire(event : LivingHurtEvent) {
		disableInFactionModFriendlyFire(event, "tacz", TaCZDamageTypes.BULLETS_TAG)
		disableInFactionModFriendlyFire(event, "superbwarfare", SuperbWarfareDamageTypes.GUN_DAMAGE)
	}


	fun disableInFactionModFriendlyFire(event : LivingHurtEvent, modId : String, damageType: TagKey<DamageType>) {
		val victim = event.entity as? ServerPlayer ?: return
		if (!(ModList.get().isLoaded(modId))) return

		val isBullet : Boolean = event.source.`is`(damageType)
		val isPlayer : Boolean = event.source.entity is ServerPlayer

		if (!isBullet && !isPlayer) return

		val attacker = event.source.entity as? ServerPlayer ?: return

		val data : FactionSavedData = FactionSavedData.getSavedData(victim.serverLevel())
		val victimFaction : Faction = data.getFactionFromPlayer(victim.uuid) ?: return
		val attackerFaction : Faction = data.getFactionFromPlayer(attacker.uuid) ?: return

		if (!victimFaction.name.equals(attackerFaction.name, true)) return

		// If these lines get reached, it doesn't matter which faction is used for getting the setting because they are the same
		val overrides = ModConfigs.commonConfig.factionSettingOverrides
		val isModdedPvpEnabled : Boolean = when (modId) {
			"tacz" -> if (overrides.doOverrideEnableTACZFriendlyFire.get()) overrides.overrideEnableTACZFriendlyFire.get()
			else victimFaction.getSettingValueByModId(modId, BooleanFactionSetting::class.java) == true
			"superbwarfare" -> if (overrides.doOverrideEnableSBWFriendlyFire.get()) overrides.overrideEnableSBWFriendlyFire.get()
			else victimFaction.getSettingValueByModId(modId, BooleanFactionSetting::class.java) == true
			else -> victimFaction.getSettingValueByModId(modId, BooleanFactionSetting::class.java) == true
		}

		if (isModdedPvpEnabled) return
		attacker.sendSystemMessage(MessageUtil.Prefix.error("You cannot attack your own faction members!"));
	}


}
