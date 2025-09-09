package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.iguanatweaksreborn.module.misc.PotionsAndEffects;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEffect.class)
public class MobEffectMixin {

	@ModifyExpressionValue(method = "isDurationEffectTick", at = @At(value = "CONSTANT", args = "intValue=25"))
	public int onPoisonTickDamage(int poisonFactor) {
		return PotionsAndEffects.getPoisonDamageSpeed(poisonFactor);
	}

	@ModifyExpressionValue(method = "applyEffectTick", at = @At(value = "CONSTANT", args = "intValue=4"))
	public int onHealFactorTick(int healFactor) {
		if (Feature.isEnabled(PotionsAndEffects.class) && PotionsAndEffects.betterHealingPotion)
			return 6;
		return healFactor;
	}

    @ModifyExpressionValue(method = "applyInstantenousEffect", at = @At(value = "CONSTANT", args = "intValue=4"))
    public int onHealFactorInstantaneous(int healFactor) {
        if (Feature.isEnabled(PotionsAndEffects.class) && PotionsAndEffects.betterHealingPotion)
            return 6;
        return healFactor;
    }

    @ModifyExpressionValue(method = "isDurationEffectTick", at = @At(value = "CONSTANT", args = "intValue=50"))
    public int onRegenTick(int regenTick) {
        return PotionsAndEffects.getRegenSpeed(regenTick);
    }
}
