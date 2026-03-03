package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.potionandeffects;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.misc.PotionsAndEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.effect.PoisonMobEffect")
public class PoisonMobEffectMixin_PotionsAndEffects {
    /**
     * Replaces the hardcoded poison tick interval (25 ticks at level I) with the value
     * from {@link PotionsAndEffects#poisonDamageSpeed}, allowing the poison damage rate
     * to be configured independently of vanilla.
     */
    @ModifyExpressionValue(method = "shouldApplyEffectTickThisTick", at = @At(value = "CONSTANT", args = "intValue=25"))
    public int insanesurvivaloverhaul$onPoisonTickDamage(int poisonFactor) {
        return PotionsAndEffects.getPoisonDamageSpeed(poisonFactor);
    }
}
