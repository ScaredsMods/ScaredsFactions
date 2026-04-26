package io.github.scaredsmods.scaredsfactionmod.event;

import io.github.scaredsmods.scaredsfactionmod.ModConfigs
import io.github.scaredsmods.scaredsfactionmod.ScaredsFactionMod
import io.github.scaredsmods.scaredsfactionmod.faction.Faction
import io.github.scaredsmods.scaredsfactionmod.faction.PersistentData
import io.github.scaredsmods.scaredsfactionmod.util.PrefixUtil
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


@Mod.EventBusSubscriber(modid = ScaredsFactionMod.MOD_ID)
object GunModImplEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun disableInFactionFriendlyFire(event : LivingHurtEvent) {
        if (!ModConfigs.commonConfig.enableTACZFriendlyFire.get()) {
            disableInFactionModFriendlyFire(event, "tacz", TaCZDamageTypes.BULLETS_TAG)
        }
        if (!ModConfigs.commonConfig.enableSBWFriendlyFire.get()) {
            disableInFactionModFriendlyFire(event, "superbwarfare", SuperbWarfareDamageTypes.GUN_DAMAGE)
        }
    }


    fun disableInFactionModFriendlyFire(event : LivingHurtEvent, modId : String, damageType: TagKey<DamageType>) {
        val victim = event.entity as? ServerPlayer ?: return
        if (!(ModList.get().isLoaded(modId))) return

        val isBullet : Boolean = event.source.`is`(damageType)
        val isPlayer : Boolean = event.source.entity is ServerPlayer

        if (!isBullet && !isPlayer) return

        val attacker : ServerPlayer = event.entity as ServerPlayer

        val data : PersistentData = PersistentData.get(victim.serverLevel())
        val victimFaction : Faction = data.getFactionByPlayer(victim.uuid)
        val attackerFaction : Faction = data.getFactionByPlayer(attacker.uuid)
        if (!victimFaction.name.equals(attackerFaction.name, true)) return

        event.isCanceled = true
        attacker.sendSystemMessage(PrefixUtil.error("You cannot attack your own faction members!"));
    }


}
