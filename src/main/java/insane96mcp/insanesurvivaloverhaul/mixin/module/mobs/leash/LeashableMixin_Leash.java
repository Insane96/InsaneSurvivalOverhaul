package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs.leash;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.mobs.Leash;
import net.minecraft.world.entity.Leashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Leashable.class)
public interface LeashableMixin_Leash {
    /**
     * Replaces the hardcoded maximum leash distance (10.0 blocks) with the configured value
     * from {@link Leash#maxDistance} when the Leash feature is enabled.
     */
    @ModifyExpressionValue(method = "tickLeash", at = @At(value = "CONSTANT", args = "doubleValue=10.0"))
    private static double insanesurvivaloverhaul$changeMaxLeashDistance(double original) {
        return Feature.isEnabled(Leash.class) ? Leash.maxDistance : original;
    }

    /**
     * Replaces the hardcoded distance (6.0 blocks) from which leashed entities are pulled towards the holder
     * with the configured value from {@link Leash#elasticDistance} when the Leash feature is enabled.
     */
    @ModifyExpressionValue(method = "tickLeash", at = @At(value = "CONSTANT", args = "doubleValue=6.0"))
    private static double insanesurvivaloverhaul$changeElasticLeashDistance(double original) {
        return Feature.isEnabled(Leash.class) ? Leash.elasticDistance : original;
    }
}
