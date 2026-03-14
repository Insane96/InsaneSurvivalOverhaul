package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth.fooddrinks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin_FoodDrinks {
	@ModifyReturnValue(at = @At("RETURN"), method = "getUseDuration")
	public int getUseDuration(int original, ItemStack stack) {
		if (FoodDrinks.eatingSpeedFormula.isEmpty()
				|| !Feature.isEnabled(FoodDrinks.class))
            return original;

		return FoodDrinks.getFoodConsumingTime(stack);
    }

	@Inject(at = @At("RETURN"), method = "getUseAnimation", cancellable = true)
	public void getUseAnimation(ItemStack stack, CallbackInfoReturnable<UseAnim> callbackInfo) {
        if (!Feature.isEnabled(FoodDrinks.class)
				|| !stack.is(FoodDrinks.DRINKING_FOODS))
			return;

        callbackInfo.setReturnValue(UseAnim.DRINK);
    }
}
