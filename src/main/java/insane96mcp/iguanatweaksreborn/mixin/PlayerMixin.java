package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import insane96mcp.iguanatweaksreborn.event.ISOEventFactory;
import insane96mcp.iguanatweaksreborn.module.combat.MiscStats;
import insane96mcp.iguanatweaksreborn.module.combat.PlayerStats;
import insane96mcp.iguanatweaksreborn.module.experience.PlayerExperience;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.healthregen.HealthRegenHunger;
import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.iguanatweaksreborn.module.world.Nether;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Map;

//Higher priority over ShieldsPlus. This makes this run first so ShieldPlus overrides this.
@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
	@Shadow
	public int experienceLevel;

	@Shadow public abstract void remove(RemovalReason pReason);

	@Shadow public abstract void resetAttackStrengthTicker();

	@Shadow public abstract void respawn();

	@Shadow public abstract void crit(Entity pEntityHit);

	@Shadow public abstract void magicCrit(Entity pEntityHit);

	@Shadow public abstract void causeFoodExhaustion(float pExhaustion);

	@Shadow public abstract void awardStat(ResourceLocation pStat, int pIncrement);

	protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
		super(type, level);
	}

	@Inject(at = @At("RETURN"), method = "getXpNeededForNextLevel", cancellable = true)
	private void xpBarCap(CallbackInfoReturnable<Integer> callback) {
		int exp = PlayerExperience.getBetterScalingLevel(this.experienceLevel);
		if (exp != -1)
			callback.setReturnValue(exp);
	}

	@Inject(at = @At("HEAD"), method = "getExperienceReward", cancellable = true)
	private void getExperiencePoints(CallbackInfoReturnable<Integer> callback) {
		int exp = PlayerExperience.getExperienceOnDeath((Player) (Object) this, false);
		if (exp != -1)
			callback.setReturnValue(exp);
	}

	@ModifyVariable(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;onPlayerAttack(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z", shift = At.Shift.BEFORE), argsOnly = true)
	public float onAttackAmount(float amount, DamageSource source) {
		return ISOEventFactory.onPlayerAttack(this, source, amount);
	}

	//Changes efficiency formula
	@ModifyVariable(method = "getDigSpeed", ordinal = 0, at = @At(value = "STORE", ordinal = 1), remap = false)
	private float iguanatweaksreborn$applyBonusEnchantmentsEfficiency(float efficiency, BlockState state, @Nullable BlockPos pos, @Local(ordinal = 0) int efficiencyLvl) {
		//Remove vanilla efficiency
		efficiency -= (float)(efficiencyLvl * efficiencyLvl + 1);
		return ISOEventFactory.getEfficiencyWithEnchantments((Player) (Object) this, state, this.getMainHandItem(), efficiency);
	}

	/**
	 * Always returns 1 when getting efficiency enchantment level so it enters the check to
	 * @param pEntity
	 * @param original
	 * @return
	 */
	@WrapOperation(method = "getDigSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getBlockEfficiency(Lnet/minecraft/world/entity/LivingEntity;)I"))
	private int iguanatweaksreborn$forceApplyBonusEnchantmentEfficiency(LivingEntity pEntity, Operation<Integer> original) {
		return 1;
	}

	@ModifyVariable(method = "actuallyHurt", at = @At(value = "STORE", ordinal = 2), argsOnly = true, ordinal = 0)
	public float iguanatweaksreborn$onPreAbsorptionCalculation(float amount, DamageSource damageSource) {
		return ISOEventFactory.onLivingHurtPreAbsorption(this, damageSource, amount);
	}

	@ModifyExpressionValue(method = "attack", at = @At(value = "CONSTANT", args = "floatValue=0.2", ordinal = 0))
	public float iguanatweaksreborn$noDamageWhenSpamming(float value) {
        return PlayerStats.noDamageWhenSpamming() ? 0f : value;
    }

	@ModifyExpressionValue(method = "attack", at = @At(value = "CONSTANT", args = "floatValue=0.8"))
	public float iguanatweaksreborn$noDamageWhenSpamming2(float value) {
		return PlayerStats.noDamageWhenSpamming() ? 1f : value;
	}

	@ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getSweepHitBox(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/AABB;"))
	public AABB iguanatweaksreborn$changeSweepingHitbox(AABB original) {
		if (!MiscStats.sweepingOverhaul
				|| !Feature.isEnabled(MiscStats.class))
			return original;
		int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING_EDGE, this);
		return original.inflate(0.5f + lvl * 0.5f, 0.05f + lvl * 0.05f, 0.5f + lvl * 0.25f);
	}

	@ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getEntityReach()D"))
	public double iguanatweaksreborn$increaseSweepingReach(double original) {
		if (!MiscStats.sweepingOverhaul
				|| !Feature.isEnabled(MiscStats.class))
			return original;
		int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING_EDGE, this);
		return original + 0.5f + lvl * 0.25f;
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

	@ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/MobType;)F"))
	public float onEnchantmentDamage(float original, Entity target) {
		Map<Enchantment, Integer> allEnchantments = this.getMainHandItem().getAllEnchantments();
		for (Enchantment enchantment : allEnchantments.keySet()) {
			original += EnchantmentsFeature.bonusDamageEnchantment(enchantment, allEnchantments.get(enchantment), this, target);
		}
		return original;
	}

	@Definition(id = "f", local = @Local(type = float.class, ordinal = 0))
	@Definition(id = "f1", local = @Local(type = float.class, ordinal = 1))
	@Expression("f + f1")
	@ModifyExpressionValue(method = "attack", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
	public float iguanatweaksreborn$increaseEnchantmentDamageWithCrit(float original, @Local(name = "flag2") boolean flag2, @Local(name = "f1") float f1, @Local CriticalHitEvent hitResult) {
		if (flag2)
			return original + (f1 * hitResult.getDamageModifier() - f1);
		return original;
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
	public boolean onCheckPeacefulRegen(boolean original) {
		return original && !HealthRegenHunger.peacefulHunger;
	}

	@ModifyExpressionValue(method = "turtleHelmetTick", at = @At(value = "CONSTANT", args = "intValue=200"))
	public int onTurtleHelmetTick(int original) {
		if (!Feature.isEnabled(Tweaks.class))
			return original;
		return Tweaks.turtleHelmetWaterBreathingTime;
	}

	@ModifyExpressionValue(method = "getPortalWaitTime", at = @At(value = "CONSTANT", args = "intValue=80"))
	public int getPortalWaitTime(int original) {
		if (!Feature.isEnabled(Nether.class))
			return original;
		return Nether.portalWaitTime;
	}

	@Inject(method = "destroyVanishingCursedItems", at = @At(value = "RETURN"))
	public void iguanatweaksreborn$onVanishingCurseItemDestroy(CallbackInfo ci) {
		if (!Feature.isEnabled(EnchantmentsFeature.class))
			return;
		EnchantmentsFeature.destroyVanishingCurseItemsInToolBelt(this);
	}

	/*@ModifyExpressionValue(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/PlayerStats;isSwimming()Z"))
	public boolean onTravelSwimCheck(boolean original) {
		if (this.isSwimming() && !this.isPassenger()) {
			this.setDeltaMovement(Vec3.ZERO);
			double d3 = this.getLookAngle().y;
			double d4 = d3 < -0.2D ? 0.085D : 0.06D;
			if (d3 <= 0.0D || !this.level().getBlockState(BlockPos.containing(this.getX(), this.getY() + 1.0D - 0.1D, this.getZ())).getFluidState().isEmpty()) {
				Vec3 vec3 = this.getDeltaMovement();
				this.setDeltaMovement(vec3.add(0.0D, (d3 - vec3.y) * d4, 0.0D));
			}
		}
		return false;
	}*/

}
