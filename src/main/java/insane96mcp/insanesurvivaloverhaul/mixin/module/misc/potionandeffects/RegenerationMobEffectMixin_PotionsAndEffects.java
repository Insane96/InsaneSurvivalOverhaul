package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.potionandeffects;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.misc.PotionsAndEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.effect.RegenerationMobEffect")
public class RegenerationMobEffectMixin_PotionsAndEffects {
    @ModifyExpressionValue(method = "shouldApplyEffectTickThisTick", at = @At(value = "CONSTANT", args = "intValue=50"))
    public int onRegenTick(int regenTick) {
        return PotionsAndEffects.getRegenSpeed(regenTick);
    }
}
