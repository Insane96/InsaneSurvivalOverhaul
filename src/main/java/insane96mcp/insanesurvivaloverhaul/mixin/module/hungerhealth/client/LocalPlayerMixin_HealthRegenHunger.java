package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.HealthRegenHunger;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin_HealthRegenHunger {
    @ModifyExpressionValue(method = "hasEnoughFoodToStartSprinting", at = @At(value = "CONSTANT", args = "floatValue=6.0"))
    public float insanesurvivaloverhaul$hasEnoughFoodToStartSprinting(float original) {
        if (!Feature.isEnabled(HealthRegenHunger.class))
            return original;

        return HealthRegenHunger.sprint$minHunger - 1;
    }
}
