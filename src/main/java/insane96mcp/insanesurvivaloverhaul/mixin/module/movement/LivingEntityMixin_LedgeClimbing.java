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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_LedgeClimbing extends Entity {
    /**
     * Air drag applied by {@link LivingEntity#travel} to the vertical movement
     */
    @Unique
    private static final double insanesurvivaloverhaul$AIR_DRAG = 0.98F;

    /**
     * True when the movement for the next tick has been set to land exactly on top of the ledge
     */
    @Unique
    private boolean insanesurvivaloverhaul$snappingToLedge = false;

    public LivingEntityMixin_LedgeClimbing(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * While pushing against a ledge that was too high to step up instantly, moves the entity upwards like on a ladder,
     * until its feet reach the top of the ledge.
     * When the next tick of climbing would go past the top, the movement is set to land exactly on the top instead,
     * and the upward speed left is then removed, so the entity ends on the ledge like when stepping up.
     */
    @ModifyReturnValue(method = "handleRelativeFrictionAndCalculateMovement", at = @At("RETURN"))
    private Vec3 insanesurvivaloverhaul$climbLedge(Vec3 original) {
        ILedgeClimber climber = (ILedgeClimber) this;
        double topY = climber.insanesurvivaloverhaul$getLedgeTopY();
        boolean wasSnapping = this.insanesurvivaloverhaul$snappingToLedge;
        this.insanesurvivaloverhaul$snappingToLedge = false;
        if (Double.isNaN(topY))
            return original;
        if (wasSnapping && this.getY() >= topY - 1.0E-5d) {
            climber.insanesurvivaloverhaul$setLedgeTopY(Double.NaN);
            return new Vec3(original.x, Math.min(original.y, 0d), original.z);
        }
        if (!Feature.isEnabled(LedgeClimbing.class)
                || !LedgeClimbing.canClimb(this)
                || !this.horizontalCollision
                || this.getY() >= topY) {
            climber.insanesurvivaloverhaul$setLedgeTopY(Double.NaN);
            return original;
        }
        this.resetFallDistance();
        //travel() subtracts gravity and applies air drag to the returned movement, giving the next tick's vertical movement
        double gravity = this.getGravity();
        double remaining = topY - this.getY();
        if ((LedgeClimbing.climbSpeed - gravity) * insanesurvivaloverhaul$AIR_DRAG >= remaining) {
            double snapY = remaining / insanesurvivaloverhaul$AIR_DRAG + gravity;
            if (original.y >= snapY)
                return original;
            this.insanesurvivaloverhaul$snappingToLedge = true;
            return new Vec3(original.x, snapY, original.z);
        }
        return new Vec3(original.x, Math.max(original.y, LedgeClimbing.climbSpeed), original.z);
    }
}
