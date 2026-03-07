package insane96mcp.insanesurvivaloverhaul.mixin.module.combat.bows;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.Bows;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin_Bows extends Projectile {

    @Shadow
    public abstract boolean isCritArrow();

    @Shadow
    private double baseDamage;

    @Shadow
    private ItemStack pickupItemStack;

    protected AbstractArrowMixin_Bows(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Cancels the crit arrow bonus damage check so fully-charged arrows don't deal extra damage.
     * @see Bows#disableCritArrowsBonusDamage
     */
    @WrapOperation(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;isCritArrow()Z", ordinal = 0))
    private boolean insanesurvivaloverhaul$cancelCritArrowDamage(AbstractArrow instance, Operation<Boolean> original) {
        if (!Feature.isEnabled(Bows.class) || !Bows.disableCritArrowsBonusDamage)
            return original.call(instance);
        return false;
    }

    /**
     * Cancels the crit arrow particle check so fully-charged arrows don't show crit particles.
     * @see Bows#disableCritArrowsBonusDamage
     */
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;isCritArrow()Z", ordinal = 0))
    private boolean insanesurvivaloverhaul$cancelCritArrowParticles(AbstractArrow instance, Operation<Boolean> original) {
        if (!Feature.isEnabled(Bows.class) || !Bows.disableCritArrowsBonusDamage)
            return original.call(instance);
        return false;
    }

    /**
     * Multiplies arrow base damage by {@link Bows#damageMultiplier}. Mob-fired arrows are unaffected.
     * @see Bows#damageMultiplier
     */
    @WrapOperation(method = "onHitEntity", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;baseDamage:D"))
    private double insanesurvivaloverhaul$applyArrowDamageMultiplier(AbstractArrow instance, Operation<Double> original) {
        if (instance.getOwner() instanceof Mob)
            return original.call(instance);
        return original.call(instance) * Bows.damageMultiplier;
    }

    /**
     * Recalculates arrow damage using decimal precision instead of rounding up, applying {@link Bows#damageMultiplier}.
     * @see Bows#decimalDamage
     */
    @ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private float insanesurvivaloverhaul$decimalDamage(float damage) {
        if (!Bows.decimalDamage)
            return damage;
        double l = this.getDeltaMovement().length();
        double damageMultiplier = Bows.damageMultiplier;
        if (this.getOwner() instanceof Mob)
            damageMultiplier = 1f;
        float newDamage = (float) Mth.clamp(l * this.baseDamage * damageMultiplier, 0.0D, Integer.MAX_VALUE);
        if (this.isCritArrow() && !Bows.disableCritArrowsBonusDamage)
            newDamage += this.random.nextFloat() * (newDamage / 2 + 2);
        return newDamage;
    }

    /**
     * Adds +1 innate pierce level to arrows fired from crossbows, on top of any Piercing enchantment.
     * @see Bows#piercingCrossbow
     */
    @WrapOperation(method = "<init>(Lnet/minecraft/world/entity/EntityType;DDDLnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getPiercingCount(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)I"))
    public int insanesurvivaloverhaul$piercingCrossbow(ServerLevel level, ItemStack firedFromWeapon, ItemStack pickupItemStack, Operation<Integer> original) {
        return Bows.piercingCrossbows(original.call(level, firedFromWeapon, this.pickupItemStack), firedFromWeapon);
    }
}
