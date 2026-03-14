package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth.fooddrinks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodProperties.class)
public class FoodPropertiesMixin_FoodDrinks {
	@ModifyReturnValue(method = "canAlwaysEat", at = @At(value = "RETURN"))
	public boolean iguanatweaksreborn$canAlwaysEat(boolean original) {
		if (!Feature.isEnabled(FoodDrinks.class)
				|| !FoodDrinks.alwaysEat)
			return original;
		return true;
	}
}
