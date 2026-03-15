package insane96mcp.insanesurvivaloverhaul.mixin.module.movement.elytranerf;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.movement.Elytra;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_ElytraNerf extends Entity {
    public LivingEntityMixin_ElytraNerf(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", ordinal = 6))
    private Vec3 checkCollideHorizontallyAndDamage(Vec3 vec3) {
        if (!Feature.isEnabled(Elytra.class)
                || this.level().dimension() == Level.END)
            return vec3;
        return vec3.multiply(Elytra.airResistance, 0.98d, Elytra.airResistance);
    }
}
