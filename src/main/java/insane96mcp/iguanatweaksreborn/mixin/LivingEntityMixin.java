package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.event.ISOEventFactory;
import insane96mcp.iguanatweaksreborn.module.combat.RegeneratingAbsorption;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.protection.IProtectionEnchantment;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.healthregen.HealthRegenHunger;
import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.iguanatweaksreborn.module.movement.BetterClimbable;
import insane96mcp.iguanatweaksreborn.module.movement.ElytraNerf;
import insane96mcp.iguanatweaksreborn.module.movement.Swimming;
import insane96mcp.iguanatweaksreborn.module.movement.TerrainSlowdown;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.tiredness.Tiredness;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable, net.minecraftforge.common.extensions.IForgeLivingEntity {

    @Shadow public abstract boolean hasEffect(MobEffect pEffect);

    @Shadow @Nullable public abstract MobEffectInstance getEffect(MobEffect pEffect);

    @Shadow public abstract Iterable<ItemStack> getArmorSlots();

    @Shadow public abstract void swing(InteractionHand pHand);

    @Shadow public abstract Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 pDeltaMovement, float pFriction);

    @Shadow @Nullable protected abstract SoundEvent getDeathSound();

    @Shadow public abstract void remove(RemovalReason pReason);

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "getCurrentSwingDuration", at = @At("RETURN"), cancellable = true)
    private void onGetCurrentSwingDuration(CallbackInfoReturnable<Integer> cir) {
        int ret = cir.getReturnValue();
        //noinspection DataFlowIssue
        if (this.hasEffect(Tiredness.TIRED.get()) && this.getEffect(Tiredness.TIRED.get()).getAmplifier() > 2)
            cir.setReturnValue(ret + 1);
    }

    @Redirect(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSprinting()Z"))
    public boolean onSprintJumpCheck(LivingEntity instance) {
        if (instance.isSprinting() && Feature.isEnabled(TerrainSlowdown.class)) {
            float yRot = instance.getYRot() * ((float)Math.PI / 180F);
            float boost = 0.2f;
            boost *= (float) MCUtils.getMovementSpeedRatio(instance);
            instance.setDeltaMovement(instance.getDeltaMovement().add((-Mth.sin(yRot) * boost), 0.0D, (Mth.cos(yRot) * boost)));
            return false;
        }
        return instance.isSprinting();
    }

    @ModifyVariable(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;onLivingAttack(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z", shift = At.Shift.BEFORE), argsOnly = true)
    public float onAttackAmount(float amount, DamageSource source) {
        return ISOEventFactory.onLivingAttack((LivingEntity) (Object) this, source, amount);
    }

    /*@ModifyVariable(method = "actuallyHurt", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    private float onCalculateAbsorption(float f1, DamageSource damageSource, float amount) {
        if (RegeneratingAbsorption.damageTypeTagOnly() && damageSource.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return amount;
        }
        return Math.max(amount - this.getAbsorptionAmount(), 0.0F);
    }

    @Redirect(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setAbsorptionAmount(F)V", ordinal = 1))
    private void onSetAbsorptionSecondTime(LivingEntity instance, float absorption) {
        //Cancel Mojang damaging absorption twice for some reason
    }*/

    @ModifyVariable(method = "actuallyHurt", at = @At(value = "STORE", ordinal = 2), argsOnly = true, ordinal = 0)
    public float onPreAbsorptionCalculation(float amount, DamageSource damageSource) {
        return ISOEventFactory.onLivingHurtPreAbsorption((LivingEntity) (Object) this, damageSource, amount);
    }

    @WrapOperation(method = "handleOnClimbable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;resetFallDistance()V"))
    public void iguanatweaksreborn$onResetFallDamageOnClimbable(LivingEntity instance, Operation<Void> original) {
        if (Feature.isEnabled(BetterClimbable.class) && BetterClimbable.fallDamageOnClimbables && instance.fallDistance > 0f)
            instance.causeFallDamage(instance.fallDistance, 0.6f, instance.damageSources().fall());
        original.call(instance);
    }

    @Inject(method = "onClimbable", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    public void iguanatweaksreborn$onCheckIfOnClimbable(CallbackInfoReturnable<Boolean> cir) {
        if (!Feature.isEnabled(BetterClimbable.class)
                || !BetterClimbable.notOnClimbableWhenOnGround)
            return;
        cir.setReturnValue(cir.getReturnValue() && !this.onGround());
    }

    @ModifyExpressionValue(method = "handleRelativeFrictionAndCalculateMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;horizontalCollision:Z"))
    public boolean iguanatweaksreborn$onCheckIfOnClimbable(boolean original) {
        if (!Feature.isEnabled(BetterClimbable.class)
                || !BetterClimbable.onlyClimbWithJump)
            return original;
        return false;
    }

    @ModifyExpressionValue(method = "handleRelativeFrictionAndCalculateMovement", at = @At(value = "CONSTANT", args = "doubleValue=0.2"))
    public double iguanatweaksreborn$climbSpeed(double original) {
        if (!Feature.isEnabled(BetterClimbable.class))
            return original;
        return BetterClimbable.climbSpeed;
    }

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    public void onPlayHurtSound(LivingEntity instance, DamageSource pSource, Operation<Void> original) {
        if (instance.getPersistentData().contains(RegeneratingAbsorption.NO_HURT_SOUND_TAG))
            return;
        original.call(instance, pSource);
    }

    @Inject(method = "getDamageAfterMagicAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageProtection(Ljava/lang/Iterable;Lnet/minecraft/world/damagesource/DamageSource;)I"), cancellable = true)
    public void onGetDamageProtection(DamageSource damageSource, float damageAmount, CallbackInfoReturnable<Float> cir) {
        if (!Feature.isEnabled(EnchantmentsFeature.class)
                || !EnchantmentsFeature.replaceProtectionEnchantments
                || damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
            return;
        MutableFloat damageReduction = new MutableFloat();
        for (ItemStack stack : this.getArmorSlots()) {
            stack.getAllEnchantments().forEach((enchantment, lvl) -> {
                if (enchantment instanceof IProtectionEnchantment protectionEnchantment && ((IProtectionEnchantment) enchantment).isSourceReduced(damageSource))
                    damageReduction.add(protectionEnchantment.getDamageReduction(lvl));
            });
        }
        if (damageReduction.getValue() == 0f)
            return;
        cir.setReturnValue(damageAmount - (damageAmount * Math.min(damageReduction.getValue(), 0.8f)));
    }

    @WrapOperation(method = "handleDamageEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    public void onPlayHurtSoundClientSide(LivingEntity instance, SoundEvent soundEvent, float volume, float pitch, Operation<Void> original, DamageSource source) {
        if (RegeneratingAbsorption.canDamageAbsorption(source) && instance.getPersistentData().getFloat(RegeneratingAbsorption.REGEN_ABSORPTION_TAG) > 0)
            return;
        original.call(instance, soundEvent, volume, pitch);
    }

    @Inject(method = "decreaseAirSupply", at = @At("RETURN"), cancellable = true)
    public void onDecreaseAirSupply(int pCurrentAir, CallbackInfoReturnable<Integer> cir) {
        if (!Feature.isEnabled(EnchantmentsFeature.class)
                || !EnchantmentsFeature.nerfRespiration)
            return;
        int respirationLvl = EnchantmentHelper.getRespiration((LivingEntity) (Object) this);
        if (respirationLvl > 0
                && this.random.nextFloat() < 1f / (1 + (respirationLvl / 2f)))
            cir.setReturnValue(pCurrentAir);
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isAffectedByFluids()Z"))
    public boolean onJumpWhenSwimmingCheck(boolean original) {
        if (!Swimming.shouldPreventFastSwimUpWithJump())
            return original;
        return original && !this.isSwimming();
    }

    @Inject(method = "setSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;removeModifier(Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V"))
    public void onRemoveSprintingModifier(boolean pSprinting, CallbackInfo ci, @Local AttributeInstance attributeInstance) {
        if (!Feature.isEnabled(EnchantmentsFeature.class)
                || HealthRegenHunger.sprintSpeedPenaltyBelowHunger == 0f)
            return;
        attributeInstance.removeModifier(HealthRegenHunger.SPRINT_PENALTY_UUID);
    }

    @Inject(method = "setSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;addTransientModifier(Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V"))
    public void onAddSprintingModifier(boolean pSprinting, CallbackInfo ci, @Local AttributeInstance attributeInstance) {
        if (!Feature.isEnabled(EnchantmentsFeature.class)
                || HealthRegenHunger.sprintSpeedPenaltyBelowHunger == 0f)
            return;
        if (!((LivingEntity)(Object)this instanceof Player player))
            return;
        float penalty = (HealthRegenHunger.sprintSpeedPenaltyBelowHunger - player.getFoodData().getFoodLevel()) * HealthRegenHunger.sprintSpeedReductionEachHunger.floatValue();
        if (penalty <= 0f)
            return;
        attributeInstance.addTransientModifier(new AttributeModifier(HealthRegenHunger.SPRINT_PENALTY_UUID, "Hungry Speed Penalty", -penalty, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @WrapOperation(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 checkCollideHorizontallyAndDamage(LivingEntity instance, Vec3 pTravelVector, float pFriction, Operation<Vec3> originalOperation) {
        return Tweaks.onCollideWithWall(instance, pTravelVector, pFriction, originalOperation);
    }

    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", ordinal = 6))
    private Vec3 checkCollideHorizontallyAndDamage(Vec3 vec3) {
        if (!Feature.isEnabled(ElytraNerf.class)
                || this.level().dimension() == Level.END)
            return vec3;
        return vec3.multiply(ElytraNerf.airResistance, 0.98d, ElytraNerf.airResistance);
    }

    @ModifyExpressionValue(method = "tryAddFrost", at = @At(value = "CONSTANT", args = "floatValue=-0.05"))
    private float onFrostSlowdown(float original) {
        if (!Feature.isEnabled(Tweaks.class))
            return original;
        return Tweaks.frozenMovementSpeedModifier.floatValue();
    }
}
