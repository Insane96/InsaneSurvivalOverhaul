package insane96mcp.insanesurvivaloverhaul.mixin.module.movement;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.movement.Swimming;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_Swimming extends Entity {
    public LivingEntityMixin_Swimming(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isAffectedByFluids()Z"))
    public boolean onJumpWhenSwimmingCheck(boolean original) {
        if (!Swimming.shouldPreventFastSwimUpWithJump()
                || !this.isInFluidType(NeoForgeMod.WATER_TYPE.value()))
            return original;
        return original && !this.isSwimming();
    }
}
