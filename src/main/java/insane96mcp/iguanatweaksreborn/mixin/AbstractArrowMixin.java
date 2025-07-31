package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.combat.MiscStats;
import insane96mcp.iguanatweaksreborn.module.combat.bows.Bows;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile {
    protected AbstractArrowMixin(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow public abstract boolean isCritArrow();

    @Shadow public abstract void setBaseDamage(double pBaseDamage);

    @Shadow private double baseDamage;

    @Shadow public abstract double getBaseDamage();

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "onHitEntity", at = @At(value = "STORE"), ordinal = 0)
    private float clampDeltaMovementLength(float deltaLength) {
        return Math.min(deltaLength, 10f);
    }

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;isCritArrow()Z", ordinal = 0))
    private boolean onCritArrowCheck(AbstractArrow arrow) {
        if (Feature.isEnabled(MiscStats.class) && Bows.disableCritArrowsBonusDamage)
            return false;
        return this.isCritArrow();
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;isCritArrow()Z", ordinal = 0))
    private boolean onCritArrowCheckParticles(boolean original) {
        if (!Feature.isEnabled(MiscStats.class) || !Bows.disableCritArrowsBonusDamage)
            return original;
        return false;
    }

    //Disable mobs' arrow random bonus damage
    @Inject(method = "setEnchantmentEffectsFromEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;setBaseDamage(D)V", ordinal = 0, shift = At.Shift.AFTER))
    private void onSetMobArrowDamage(LivingEntity pShooter, float pVelocity, CallbackInfo ci) {
        this.setBaseDamage(pVelocity * this.baseDamage);
    }

    @WrapOperation(method = "onHitEntity", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;baseDamage:D"))
    private double onSetArrowDamage(AbstractArrow instance, Operation<Double> original) {
        if (instance.getOwner() instanceof Mob)
            return original.call(instance);
        return original.call(instance) * Bows.damageMultiplier;
    }

    @ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private float onHurtDamage(float damage) {
        if (!Bows.floatPointDamage)
            return damage;
        double l = this.getDeltaMovement().length();
        double damageMultiplier = Bows.damageMultiplier;
        if (this.getOwner() instanceof Mob)
            damageMultiplier = 1f;
        float newDamage = (float) Mth.clamp(l * this.baseDamage * damageMultiplier, 0.0D, Integer.MAX_VALUE);
        if (this.isCritArrow() && !Bows.disableCritArrowsBonusDamage) {
            newDamage += this.random.nextFloat() * (newDamage / 2 + 2);
        }
        return newDamage;
    }

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;doPostHurtEffects(Lnet/minecraft/world/entity/LivingEntity;)V", shift = At.Shift.AFTER))
    public void onHitEntity(EntityHitResult pResult, CallbackInfo ci) {
        if (!(pResult.getEntity() instanceof Player) && this.getOwner() instanceof ServerPlayer serverPlayer && serverPlayer.distanceToSqr(pResult.getEntity()) >= Tweaks.dingDistance * Tweaks.dingDistance) {
            serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
        }
    }

    @Definition(id = "getBaseDamage", method = "Lnet/minecraft/world/entity/projectile/AbstractArrow;getBaseDamage()D")
    @Expression("this.getBaseDamage() + (double) ? * ? + ?")
    @ModifyExpressionValue(method = "setEnchantmentEffectsFromEntity", at = @At("MIXINEXTRAS:EXPRESSION"))
    public double iguanatweaksreborn$setBaseDamage(double pBaseDamage, @Local(ordinal = 0) int powerLvl) {
        if (!EnchantmentsFeature.powerAffectsBaseArrowDamage && EnchantmentsFeature.powerEnchantmentDamage == 0.5d)
            return pBaseDamage;
        if (EnchantmentsFeature.powerAffectsBaseArrowDamage)
            return this.getBaseDamage() + (this.getBaseDamage() * EnchantmentsFeature.powerEnchantmentDamage * powerLvl);
        else
            return this.getBaseDamage() + EnchantmentsFeature.powerEnchantmentDamage + EnchantmentsFeature.powerEnchantmentDamage * powerLvl;
    }

    @ModifyExpressionValue(method = "onHitEntity", at = @At(value = "CONSTANT", args = "doubleValue=0.6"))
    public double iguanatweaksreborn$setPunchStrength(double strength) {
        if (!Feature.isEnabled(EnchantmentsFeature.class))
            return strength;
        return EnchantmentsFeature.punchStrength;
    }
}
