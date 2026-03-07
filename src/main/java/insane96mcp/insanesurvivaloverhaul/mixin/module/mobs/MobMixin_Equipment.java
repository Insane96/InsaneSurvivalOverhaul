package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs;

import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.mobs.equipment.Equipment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Mob.class)
public abstract class MobMixin_Equipment extends LivingEntity {

    protected MobMixin_Equipment(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    /**
     * Raises the drop-chance threshold in the experience reward check from 1.0 to 100.0
     * so that mobs with a drop chance above 1.0 (set by Equipment) still award bonus XP.
     */
    @ModifyConstant(method = "getBaseExperienceReward", constant = @Constant(floatValue = 1f))
    public float insanesurvivaloverhaul$equipmentXpDropChanceThreshold(float original) {
        return 100f;
    }

    /**
     * When Equipment is enabled, randomises the dropped item's damage value between 0 and
     * {@code maxDurability * maxDamage} instead of clamping to {@code maxDamage}.
     * When disabled, still clamps to {@code maxDamage} to prevent negative durability.
     */
    @ModifyArg(method = "dropCustomDeathLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V"))
    public int insanesurvivaloverhaul$equipmentDroppedDurability(int damageValue, @Local ItemStack stack) {
        if (!Feature.isEnabled(Equipment.class))
            return Math.min(damageValue, stack.getMaxDamage());
        return stack.getMaxDamage() - this.random.nextInt((int) (stack.getMaxDamage() * Equipment.maxDurability));
    }
}
