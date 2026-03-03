package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin_Tweaks {
    /**
     * Wraps the horizontal movement calculation to detect wall collisions and optionally
     * apply damage to the entity when it runs into a wall at sufficient speed.
     */
    @WrapOperation(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 insanesurvivaloverhaul$checkCollideHorizontallyAndDamage(LivingEntity instance, Vec3 pTravelVector, float pFriction, Operation<Vec3> originalOperation) {
        return Tweaks.onCollideWithWall(instance, pTravelVector, pFriction, originalOperation);
    }

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
