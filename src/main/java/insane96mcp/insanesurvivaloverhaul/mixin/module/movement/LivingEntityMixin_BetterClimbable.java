package insane96mcp.insanesurvivaloverhaul.mixin.module.movement;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.movement.BetterClimbable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_BetterClimbable extends Entity {
    public LivingEntityMixin_BetterClimbable(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "handleOnClimbable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;resetFallDistance()V"))
    public void iguanatweaksreborn$onResetFallDamageOnClimbable(LivingEntity instance, Operation<Void> original) {
        if (Feature.isEnabled(BetterClimbable.class) && BetterClimbable.fallDamageOnClimbable && instance.fallDistance > 0f)
            instance.causeFallDamage(instance.fallDistance, 0.75f, instance.damageSources().fall());
        original.call(instance);
    }

    @Inject(method = "onClimbable", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    public void iguanatweaksreborn$onCheckIfOnClimbable(CallbackInfoReturnable<Boolean> cir) {
        if (!Feature.isEnabled(BetterClimbable.class)
                || !BetterClimbable.notOnClimbableWhenOnGround)
            return;
        cir.setReturnValue(cir.getReturnValue() && !this.onGround());
    }

    @ModifyExpressionValue(method = "handleRelativeFrictionAndCalculateMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;horizontalCollision:Z", opcode = Opcodes.GETFIELD))
    public boolean iguanatweaksreborn$onCheckIfOnClimbable(boolean original) {
        if (!Feature.isEnabled(BetterClimbable.class)
                || !BetterClimbable.onlyClimbWithJump
                || !((Object) this instanceof Player))
            return original;
        return false;
    }

    @ModifyExpressionValue(method = "handleRelativeFrictionAndCalculateMovement", at = @At(value = "CONSTANT", args = "doubleValue=0.2"))
    public double iguanatweaksreborn$climbSpeed(double original) {
        if (!Feature.isEnabled(BetterClimbable.class))
            return original;
        return BetterClimbable.climbSpeed;
    }
}
