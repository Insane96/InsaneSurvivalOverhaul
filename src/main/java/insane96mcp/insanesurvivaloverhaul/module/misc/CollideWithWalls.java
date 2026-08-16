package insane96mcp.insanesurvivaloverhaul.module.misc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@LoadFeature(module = ISOModules.MISC, description = "Colliding with walls at high speed (e.g. from explosions or knockback) deals damage.")
public class CollideWithWalls extends Feature {

    public static ResourceKey<DamageType> COLLIDE_WITH_WALL = ResourceKey.create(Registries.DAMAGE_TYPE, InsaneSO.id("collide_with_wall"));

    @Config(min = 0, description = "If set higher than 0 it will enable damage when colliding with walls at a high speed (e.g. with explosions or knockback). Higher = more damage. Set to 0 to disable. Please note that even if set to 0, might still show up in performance profilers.")
    public static Double damage = 3d;

    public static Vec3 onCollideWithWall(LivingEntity living, Vec3 pTravelVector, float pFriction, Operation<Vec3> originalOperation) {
        if (!Feature.isEnabled(CollideWithWalls.class)
                || damage == 0
                || (living instanceof Mob mob && mob.isLeashed()))
            return originalOperation.call(living, pTravelVector, pFriction);
        Vec3 oldDeltaMovement = living.getDeltaMovement();
        double horizontalDistance = oldDeltaMovement.horizontalDistance();
        Vec3 originalResult = originalOperation.call(living, pTravelVector, pFriction);
        if (living.horizontalCollision && !living.level().isClientSide) {
            double length = horizontalDistance - living.getDeltaMovement().horizontalDistance();
            if (length > 0.6f) {
                living.hurt(living.damageSources().source(CollideWithWalls.COLLIDE_WITH_WALL, null), (float) ((length - 0.6f) * damage));

                if (!living.level().isClientSide) {
                    double x = living.getX();
                    double y = living.getY();
                    double z = living.getZ();
                    Direction direction = Direction.getNearest(oldDeltaMovement.x, oldDeltaMovement.y, oldDeltaMovement.z);
                    BlockPos pos = BlockPos.containing(x, y, z).relative(direction);
                    BlockState state = living.level().getBlockState(pos);
                    if (state.isAir()) {
                        int height = Mth.ceil(living.getBbHeight());
                        for (int i = 1; i < height; i++) {
                            pos = pos.above();
                            state = living.level().getBlockState(pos);
                            if (!state.isAir())
                                break;
                        }
                    }
                    if (state.isAir())
                        return originalResult;

                    int particleCount = 150;
                    living.playSound(living.getFallSounds().big(), 1.0F, 0.7F);
                    ((ServerLevel) living.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state).setPos(pos), x, living.getY() + living.getBbHeight() / 2f, z, particleCount, 0.0D, 0.0D, 0.0D, 0.15F);
                }
            }
        }
        return originalResult;
    }
}
