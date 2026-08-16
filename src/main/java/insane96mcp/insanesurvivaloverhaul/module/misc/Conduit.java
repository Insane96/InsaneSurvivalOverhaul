package insane96mcp.insanesurvivaloverhaul.module.misc;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@LoadFeature(module = ISOModules.MISC, description = "Greatly increases the range and damage of the conduit")
public class Conduit extends Feature {
    @Config(min = 0d, max = 64d, description = "Distance multiplier (formula is `blocks_around / 7 * this_multiplier`) from the conduit at which it will deal damage to enemies.")
    public static Double protectionDistanceMultiplier = 8d;
    @Config(min = 0d, max = 96d, description = "If a mob is within this radius from the conduit, it will be dealt the maximum amount of damage.")
    public static Double protectionMaxDamageDistance = 8d;
    @Config(description = "If true, conduit effect will no longer speed up mining speed.")
    public static Boolean removeHaste = true;

    private static final float MIN_DAMAGE = 2f;
    private static final float MAX_DAMAGE = 6f;

    public static boolean conduitUpdateDestroyEnemies(Level level, BlockPos blockPos, List<BlockPos> blocks) {
        if (!isEnabled(Conduit.class))
            return false;

        LivingEntity nearestEntity = level.getNearestEntity(LivingEntity.class, TargetingConditions.forNonCombat().selector(livingEntity -> livingEntity instanceof Enemy && livingEntity.isInWaterOrRain()), null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), getDamageAABB(blockPos, blocks));
        if (nearestEntity == null)
            return true;

        level.playSound(null, nearestEntity.getX(), nearestEntity.getY(), nearestEntity.getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.BLOCKS, 1.0F, 1.0F);
        double distance = nearestEntity.position().distanceTo(new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
        float damage;
        if (distance < protectionMaxDamageDistance)
            damage = MAX_DAMAGE;
        else
            damage = (float) (1 - (distance - protectionMaxDamageDistance) / (maxRangeRadius() - protectionMaxDamageDistance)) * (MAX_DAMAGE - MIN_DAMAGE) + MIN_DAMAGE;
        nearestEntity.hurt(nearestEntity.damageSources().magic(), damage);
        return true;
    }

    private static AABB getDamageAABB(BlockPos blockPos, List<BlockPos> blocks) {
        double range = blocks.size() / 7d * protectionDistanceMultiplier;
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        return (new AABB(x, y, z, x + 1, y + 1, z + 1)).inflate(range);
    }

    private static double maxRange() {
        return 42 / 7d * protectionDistanceMultiplier;
    }

    private static double maxRangeRadius() {
        return Math.sqrt(maxRange() * maxRange() + maxRange() * maxRange());
    }

    public static boolean shouldRemoveConduitHaste() {
        return isEnabled(Conduit.class) && removeHaste;
    }
}
