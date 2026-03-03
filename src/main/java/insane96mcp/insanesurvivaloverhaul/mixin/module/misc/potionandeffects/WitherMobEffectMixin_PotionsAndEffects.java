package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.potionandeffects;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.misc.PotionsAndEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.effect.WitherMobEffect")
public class WitherMobEffectMixin_PotionsAndEffects {
    /**
     * Replaces the hardcoded Wither tick interval (40 ticks at level I) with the value
     * from {@link PotionsAndEffects#poisonDamageSpeed}, tying the Wither damage rate
     * to the same configurable speed as Poison.
     */
    @ModifyExpressionValue(method = "shouldApplyEffectTickThisTick", at = @At(value = "CONSTANT", args = "intValue=40"))
    public int insanesurvivaloverhaul$onWitherTickDamage(int witherFactor) {
        return PotionsAndEffects.getPoisonDamageSpeed(witherFactor);
    }
}
