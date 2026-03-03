package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.world.entity.Leashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Leashable.class)
public abstract class LeashableMixin_Tweaks {
    /**
     * Replaces the hardcoded maximum leash distance (10.0 blocks) with the configured value
     * from {@link Tweaks#leashMaxDistance} when the Tweaks feature is enabled.
     */
    @ModifyExpressionValue(method = "tickLeash", at = @At(value = "CONSTANT", args = "doubleValue=10.0"))
    private static double insanesurvivaloverhaul$changeMaxLeashDistance(double original) {
        return Feature.isEnabled(Tweaks.class) ? Tweaks.leashMaxDistance : original;
    }
}
