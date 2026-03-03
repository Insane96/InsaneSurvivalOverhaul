package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.potionandeffects;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.misc.PotionsAndEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.effect.RegenerationMobEffect")
public class RegenerationMobEffectMixin_PotionsAndEffects {
    /**
     * Replaces the hardcoded regeneration tick interval (50 ticks at level I) with the
     * value from {@link PotionsAndEffects#regenerationBaseSpeed}, allowing the heal rate
     * of the Regeneration effect to be configured independently of vanilla.
     */
    @ModifyExpressionValue(method = "shouldApplyEffectTickThisTick", at = @At(value = "CONSTANT", args = "intValue=50"))
    public int insanesurvivaloverhaul$onRegenTick(int regenTick) {
        return PotionsAndEffects.getRegenSpeed(regenTick);
    }
}
