package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodProperties.class)
public class FoodPropertiesMixin {
	@ModifyReturnValue(method = "canAlwaysEat", at = @At(value = "RETURN"))
	public boolean iguanatweaksreborn$canAlwaysEat(boolean original) {
		if (!Feature.isEnabled(FoodDrinks.class)
				|| !FoodDrinks.alwaysEat)
			return original;
		return true;
	}
}
