package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin_Tweaks {
    /**
     * Replaces the hardcoded frozen movement speed modifier (-0.05) with the configured
     * value from {@link Tweaks#frozenMovementSpeedModifier} when the Tweaks feature is enabled.
     */
    @ModifyExpressionValue(method = "tryAddFrost", at = @At(value = "CONSTANT", args = "floatValue=-0.05"))
    private float insanesurvivaloverhaul$onFrostSlowdown(float original) {
        if (!Feature.isEnabled(Tweaks.class))
            return original;
        return Tweaks.frozenMovementSpeedModifier.floatValue();
    }
}
