package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.collidewithwalls;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanesurvivaloverhaul.module.misc.CollideWithWalls;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin_CollideWithWalls {
    /**
     * Wraps the horizontal movement calculation to detect wall collisions and optionally
     * apply damage to the entity when it runs into a wall at sufficient speed.
     */
    @WrapOperation(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 insanesurvivaloverhaul$checkCollideHorizontallyAndDamage(LivingEntity instance, Vec3 pTravelVector, float pFriction, Operation<Vec3> originalOperation) {
        return CollideWithWalls.onCollideWithWall(instance, pTravelVector, pFriction, originalOperation);
    }
}
