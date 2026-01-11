package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.event.ISOEventFactory;
import insane96mcp.iguanatweaksreborn.module.combat.PlayerStats;
import insane96mcp.iguanatweaksreborn.module.combat.Shields;
import insane96mcp.iguanatweaksreborn.module.experience.PlayerExperience;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.healthregen.HealthRegenHunger;
import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.iguanatweaksreborn.module.world.Nether;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
		return ISOEventFactory.getMiningSpeedWithEnchantments((Player) (Object) this, state, this.getMainHandItem(), efficiency, false);
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
		return Tweaks.turtle$helmetWaterBreathingTime;
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

	@ModifyExpressionValue(method = "hurtCurrentlyUsedShield", at = @At(value = "CONSTANT", args = "floatValue=3.0"))
	private float shieldsPlus$blockingWindupTime(float minDamage) {
		return Shields.getMinHurtDamage(minDamage);
	}
}
