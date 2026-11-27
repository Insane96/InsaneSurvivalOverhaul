package insane96mcp.insanesurvivaloverhaul.mixin.module.movement;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanesurvivaloverhaul.module.movement.ILedgeClimber;
import insane96mcp.insanesurvivaloverhaul.module.movement.LedgeClimbing;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin_LedgeClimbing implements ILedgeClimber {

    @Unique
    private double insanesurvivaloverhaul$ledgeTopY = Double.NaN;

    @Override
    public double insanesurvivaloverhaul$getLedgeTopY() {
        return this.insanesurvivaloverhaul$ledgeTopY;
    }

    @Override
    public void insanesurvivaloverhaul$setLedgeTopY(double y) {
        this.insanesurvivaloverhaul$ledgeTopY = y;
    }

    /**
     * Prevents step up candidates higher than {@link LedgeClimbing#maxInstantStepHeight}. If the step up would have moved the entity,
     * the top of the ledge is stored, so the entity can climb it in {@link LivingEntityMixin_LedgeClimbing}.
     * Returning {@link Vec3#ZERO} makes vanilla discard the candidate, as it never moves further horizontally than without stepping.
     */
    @WrapOperation(method = "collide", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;collideWithShapes(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/List;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 insanesurvivaloverhaul$preventHighStepUp(Vec3 movement, AABB box, List<VoxelShape> shapes, Operation<Vec3> original) {
        Vec3 result = original.call(movement, box, shapes);
        if (!LedgeClimbing.shouldClimb((Entity) (Object) this, movement.y))
            return result;
        if (result.horizontalDistanceSqr() > 1.0E-7)
            this.insanesurvivaloverhaul$ledgeTopY = box.minY + movement.y;
        return Vec3.ZERO;
    }
}
