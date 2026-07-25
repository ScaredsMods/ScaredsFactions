package io.github.scaredsmods.scaredsfactions.common.mixin.client;


import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.BooleanFactionSetting;
import io.github.scaredsmods.scaredsfactions.common.faction.ClientFactionSavedData;
import io.github.scaredsmods.scaredsfactions.common.faction.Faction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void shouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && ClientFactionSavedData.isEqualFaction(player.getUUID())) {
            Faction faction = ClientFactionSavedData.getFactionFromPlayer(player.getUUID());
            if (faction != null) {
                Boolean glowEnabled = faction.getSettingValue("enableFriendlyGlow", BooleanFactionSetting.class);
                if (glowEnabled != null && glowEnabled) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}