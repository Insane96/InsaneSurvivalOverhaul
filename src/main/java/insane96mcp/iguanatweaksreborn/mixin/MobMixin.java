package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.mobs.equipment.Equipment;
import insane96mcp.iguanatweaksreborn.module.world.weather.Weather;
import insane96mcp.iguanatweaksreborn.module.world.weather.WeatherSavedData;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {
	protected MobMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	//Fixes mobs not dropping bonus experience if the drop chance of armor/held items is set higher than 1f
	@ModifyConstant(method = "getExperienceReward", constant = @Constant(floatValue = 1f))
	public float onDropChanceCheck(float dropChance) {
		return 100f;
	}

	@ModifyArg(method = "dropCustomDeathLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V"))
	public int damageValue(int damageValue, @Local ItemStack stack) {
		if (!Feature.isEnabled(Equipment.class))
			return Math.min(damageValue, stack.getMaxDamage());
		return stack.getMaxDamage() - this.random.nextInt((int) (stack.getMaxDamage() * Equipment.maxDurability));
	}

	@Inject(method = "isSunBurnTick", at = @At(value = "HEAD"), cancellable = true)
	public void damageValue(CallbackInfoReturnable<Boolean> cir) {
		if (!(level() instanceof ServerLevel serverLevel)
				|| !serverLevel.getGameRules().getBoolean(Weather.RULE_FOGGYWEATHER))
			return;

		WeatherSavedData.FoggyData foggyData = Weather.getCurrentFoggyData(serverLevel);
		if (foggyData.current.ordinal() >= 4 && (foggyData.target.ordinal() >= 4 || foggyData.getRatioToTarget() < 0.5f))
			cir.setReturnValue(false);
	}
}
