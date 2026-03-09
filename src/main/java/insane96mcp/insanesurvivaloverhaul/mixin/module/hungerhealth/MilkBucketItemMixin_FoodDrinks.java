package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MilkBucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MilkBucketItem.class)
public class MilkBucketItemMixin_FoodDrinks {
	@Inject(at = @At("RETURN"), method = "getUseDuration", cancellable = true)
	public void getUseDuration(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
		if (!FoodDrinks.fasterDrinkConsuming
				|| !Feature.isEnabled(FoodDrinks.class))
			return;

		cir.setReturnValue(20);
	}
}
