package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.potionandeffects;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanesurvivaloverhaul.module.misc.PotionsAndEffects;
import net.minecraft.world.entity.projectile.ThrownPotion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownPotion.class)
public class ThrownPotionMixin_PotionsAndEffects {

    /**
     * Replaces the base splash potion effect strength multiplier (1.0) with
     * {@link PotionsAndEffects#STREAMLINE_SPLASH_POTION_MULTIPLIER} when streamlined splash
     * potions are enabled, making all splash potions consistently weaker than drinkable ones.
     */
    @ModifyExpressionValue(method = "applySplash", at = @At(value = "CONSTANT", args = "doubleValue=1.0", ordinal = 0))
    public double insanesurvivaloverhaul$applySplashBaseModifier(double original) {
        return PotionsAndEffects.streamlineSplashPotions() ? PotionsAndEffects.STREAMLINE_SPLASH_POTION_MULTIPLIER : original;
    }

    /**
     * Replaces the distance-based splash potion effect strength expression
     * ({@code 1.0 - sqrt(d0) / 4.0}) with {@link PotionsAndEffects#STREAMLINE_SPLASH_POTION_MULTIPLIER}
     * when streamlined splash potions are enabled, removing the distance falloff so all
     * nearby entities receive the full (streamlined) effect strength.
     */
    @Definition(id = "sqrt", method = "Ljava/lang/Math;sqrt(D)D")
    @Definition(id = "d0", local = @Local(type = double.class, ordinal = 0))
    @Expression("1.0 - sqrt(d0) / 4.0")
    @ModifyExpressionValue(method = "applySplash", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public double insanesurvivaloverhaul$applySplashDistanceModifier(double original) {
        return PotionsAndEffects.streamlineSplashPotions() ? PotionsAndEffects.STREAMLINE_SPLASH_POTION_MULTIPLIER : original;
    }
}
