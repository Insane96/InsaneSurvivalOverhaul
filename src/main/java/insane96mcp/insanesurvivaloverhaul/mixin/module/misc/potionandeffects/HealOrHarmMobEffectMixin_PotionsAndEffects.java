package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.potionandeffects;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.misc.PotionsAndEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.effect.HealOrHarmMobEffect")
public class HealOrHarmMobEffectMixin_PotionsAndEffects {
    /**
     * Replaces the hardcoded heal/harm factor (6 health per level) used during periodic
     * ticks with 4 when {@link PotionsAndEffects#streamlineHealAndHarmPotions} is enabled,
     * making Instant Health and Instant Damage scale more consistently.
     */
    @ModifyExpressionValue(method = "applyEffectTick", at = @At(value = "CONSTANT", args = "intValue=6"))
    public int insanesurvivaloverhaul$onHealFactorTick(int healFactor) {
        if (Feature.isEnabled(PotionsAndEffects.class) && PotionsAndEffects.streamlineHealAndHarmPotions)
            return 4;
        return healFactor;
    }

    /**
     * Replaces the hardcoded instantaneous heal/harm factor (6 health per level) with 4
     * when {@link PotionsAndEffects#streamlineHealAndHarmPotions} is enabled, keeping the
     * behaviour consistent between tick-based and instant applications.
     */
    @ModifyExpressionValue(method = "applyInstantenousEffect", at = @At(value = "CONSTANT", args = "intValue=6"))
    public int insanesurvivaloverhaul$onHealFactorInstantaneous(int healFactor) {
        if (Feature.isEnabled(PotionsAndEffects.class) && PotionsAndEffects.streamlineHealAndHarmPotions)
            return 4;
        return healFactor;
    }
}
