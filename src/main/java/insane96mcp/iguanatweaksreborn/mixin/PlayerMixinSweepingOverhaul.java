package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import insane96mcp.iguanatweaksreborn.module.combat.MiscStats;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixinSweepingOverhaul extends LivingEntity {

    protected PlayerMixinSweepingOverhaul(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow
    public abstract void crit(Entity pEntityHit);

    @Shadow public abstract void magicCrit(Entity pEntityHit);

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V", ordinal = 1, shift = At.Shift.AFTER))
    public void iguanatweaksreborn$changeSweepingDamage(Entity pTarget, CallbackInfo ci, @Local(ordinal = 0) float f, @Local(ordinal = 5) LocalFloatRef f3) {
        if (!MiscStats.sweepingOverhaul
                || !Feature.isEnabled(MiscStats.class))
            return;
        f3.set(f);
    }

    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getSweepHitBox(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/AABB;"))
    public AABB iguanatweaksreborn$changeSweepingHitbox(AABB original) {
        if (!MiscStats.sweepingOverhaul
                || !Feature.isEnabled(MiscStats.class))
            return original;
        return original.inflate(1.5f, 0.15f, 1.5f);
    }

    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D", ordinal = 2))
    public double iguanatweaksreborn$increaseSweepingReach(double original) {
        if (!MiscStats.sweepingOverhaul
                || !Feature.isEnabled(MiscStats.class))
            return original;
        return original + 1f;
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", shift = At.Shift.AFTER))
    public void iguanatweaksreborn$onSweepHurt(Entity pTarget, CallbackInfo ci, @Local(name = "flag") boolean flag, @Local(name = "flag2") boolean flag2, @Local(name = "flag3") boolean flag3, @Local(name = "f1") float f1, @Local(name = "f4") float f4, @Local(name = "j") int fireAspect, @Local LivingEntity sweepTarget) {
        if (flag2) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, this.getSoundSource(), 1.0F, 1.0F);
            this.crit(sweepTarget);
        }

        if (!flag2 && !flag3) {
            if (flag)
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, this.getSoundSource(), 1.0F, 1.0F);
            else
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, this.getSoundSource(), 1.0F, 1.0F);
        }

        if (f1 > 0.0F)
            this.magicCrit(sweepTarget);

        EnchantmentHelper.doPostHurtEffects(sweepTarget, this);
        EnchantmentHelper.doPostDamageEffects(this, sweepTarget);

        //float damageDealt = f4 - living.getHealth();
        //this.awardStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
        if (fireAspect > 0) {
            sweepTarget.setSecondsOnFire(fireAspect * 4);
        }

        //if (this.level() instanceof ServerLevel serverLevel && damageDealt > 2.0F) {
        //	int k = (int)((double)damageDealt * 0.5D);
        //	serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, pTarget.getX(), pTarget.getY(0.5D), pTarget.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
        //}
    }

	/*@ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/PlayerStats;onGround()Z", ordinal = 1))
	public boolean allowSweepingOffGround(boolean original) {
		return true;
	}*/

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getFireAspect(Lnet/minecraft/world/entity/LivingEntity;)I"))
    public void storeNewFlag3(Entity pTarget, CallbackInfo ci, @Local(ordinal = 0) boolean flag, @Share("flag3") LocalBooleanRef flag3) {
        if (!MiscStats.sweepingOverhaul
                || !Feature.isEnabled(MiscStats.class))
            return;
        if (flag) {
            ItemStack itemstack = this.getItemInHand(InteractionHand.MAIN_HAND);
            flag3.set(itemstack.canPerformAction(net.minecraftforge.common.ToolActions.SWORD_SWEEP));
        }
    }

    @ModifyVariable(method = "attack", ordinal = 3, at = @At("LOAD"))
    public boolean onFlag3Check(boolean original, @Share("flag3") LocalBooleanRef flag3) {
        if (!MiscStats.sweepingOverhaul
                || !Feature.isEnabled(MiscStats.class))
            return original;
        return flag3.get();
    }

    @ModifyExpressionValue(method = "attack",at = @At(value = "CONSTANT", args = "doubleValue=0.4000000059604645"))
    public double onSweepKnockbackStrength(double original, @Local(name = "i") float i) {
        if (!MiscStats.sweepingOverhaul
                || !Feature.isEnabled(MiscStats.class))
            return original;
        return i * 0.5F;
    }
}
