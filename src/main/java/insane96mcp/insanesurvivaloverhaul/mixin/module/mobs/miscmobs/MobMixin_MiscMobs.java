package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs.miscmobs;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanesurvivaloverhaul.module.mobs.MiscMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
public abstract class MobMixin_MiscMobs extends LivingEntity {

    protected MobMixin_MiscMobs(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @WrapOperation(method = "isSunBurnTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;canSeeSky(Lnet/minecraft/core/BlockPos;)Z"))
    public boolean insanesurvivaloverhaul$burnInSkyLight(Level instance, BlockPos blockPos, Operation<Boolean> original) {
        if (!MiscMobs.isBurnInSkyLight())
            return original.call(instance, blockPos);
        return instance.getBrightness(LightLayer.SKY, blockPos) >= 7;
    }
}
