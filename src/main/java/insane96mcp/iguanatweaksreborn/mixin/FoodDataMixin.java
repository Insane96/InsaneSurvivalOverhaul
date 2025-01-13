package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.healthregen.HealthRegen;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FoodData.class)
public class FoodDataMixin {
	@Shadow public float saturationLevel;

	@Shadow public int foodLevel;

	@Inject(at = @At("HEAD"), method = "tick", cancellable = true)
	public void onTick(Player player, CallbackInfo callbackInfo) {
		if (HealthRegen.tickFoodStats((FoodData) (Object) this, player))
			callbackInfo.cancel();
	}

	@WrapOperation(method = "eat(IF)V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F"))
	public float onEat(float a, float b, Operation<Float> original, int pFoodLevelModifier, float pSaturationLevelModifier) {
		if (Feature.isEnabled(FoodDrinks.class)
				&& FoodDrinks.combatSnapshotEatingSaturation) {
			float saturationRestored = pFoodLevelModifier * pSaturationLevelModifier * 2.0F;
			return Math.min(Math.max(this.saturationLevel, saturationRestored), (float) this.foodLevel);
		}
		return original.call(a, b);
	}
}
