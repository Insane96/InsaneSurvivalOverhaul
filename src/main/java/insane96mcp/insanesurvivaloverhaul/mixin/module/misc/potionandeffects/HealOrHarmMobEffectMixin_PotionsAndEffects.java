package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.potionandeffects;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.misc.PotionsAndEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.effect.HealOrHarmMobEffect")
public class HealOrHarmMobEffectMixin_PotionsAndEffects {
    @ModifyExpressionValue(method = "applyEffectTick", at = @At(value = "CONSTANT", args = "intValue=6"))
    public int onHealFactorTick(int healFactor) {
        if (Feature.isEnabled(PotionsAndEffects.class) && PotionsAndEffects.streamlineHealAndHarmPotions)
            return 4;
        return healFactor;
    }

    @ModifyExpressionValue(method = "applyInstantenousEffect", at = @At(value = "CONSTANT", args = "intValue=6"))
    public int onHealFactorInstantaneous(int healFactor) {
        if (Feature.isEnabled(PotionsAndEffects.class) && PotionsAndEffects.streamlineHealAndHarmPotions)
            return 4;
        return healFactor;
    }
}
