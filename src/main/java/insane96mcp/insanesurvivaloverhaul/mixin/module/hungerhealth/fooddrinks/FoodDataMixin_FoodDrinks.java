package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth.fooddrinks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FoodData.class)
public class FoodDataMixin_FoodDrinks {
    @Shadow
    private float saturationLevel;

    @WrapOperation(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
	public float onEat(float value, float min, float max, Operation<Float> original, int foodLevel, float saturationLevel) {
        if (!Feature.isEnabled(FoodDrinks.class)
                || !FoodDrinks.combatSnapshotEatingSaturation)
            return original.call(value, min, max);
        return original.call(Math.max(this.saturationLevel, saturationLevel), min, max);
    }
}
