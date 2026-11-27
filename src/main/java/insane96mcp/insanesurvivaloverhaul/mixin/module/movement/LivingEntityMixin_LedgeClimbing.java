package insane96mcp.insanesurvivaloverhaul.mixin.module.movement;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.movement.ILedgeClimber;
import insane96mcp.insanesurvivaloverhaul.module.movement.LedgeClimbing;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_LedgeClimbing extends Entity {
    public LivingEntityMixin_LedgeClimbing(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * While pushing against a ledge that was too high to step up instantly, moves the entity upwards like on a ladder,
     * until its feet reach the top of the ledge.
     */
    @ModifyReturnValue(method = "handleRelativeFrictionAndCalculateMovement", at = @At("RETURN"))
    private Vec3 insanesurvivaloverhaul$climbLedge(Vec3 original) {
        ILedgeClimber climber = (ILedgeClimber) this;
        double topY = climber.insanesurvivaloverhaul$getLedgeTopY();
        if (Double.isNaN(topY))
            return original;
        if (!Feature.isEnabled(LedgeClimbing.class)
                || !LedgeClimbing.canClimb(this)
                || !this.horizontalCollision
                || this.getY() >= topY) {
            climber.insanesurvivaloverhaul$setLedgeTopY(Double.NaN);
            return original;
        }
        this.resetFallDistance();
        return new Vec3(original.x, Math.max(original.y, LedgeClimbing.climbSpeed), original.z);
    }
}
