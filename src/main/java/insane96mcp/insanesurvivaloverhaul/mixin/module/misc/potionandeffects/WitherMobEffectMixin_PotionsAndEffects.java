package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.potionandeffects;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.misc.PotionsAndEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.effect.WitherMobEffect")
public class WitherMobEffectMixin_PotionsAndEffects {
    @ModifyExpressionValue(method = "shouldApplyEffectTickThisTick", at = @At(value = "CONSTANT", args = "intValue=40"))
    public int onPoisonTickDamage(int poisonFactor) {
        return PotionsAndEffects.getPoisonDamageSpeed(poisonFactor);
    }
}
