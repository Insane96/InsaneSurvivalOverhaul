package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.iguanatweaksreborn.module.items.unbreakableitems.UnbreakableItems;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
	@Inject(at = @At("HEAD"), method = "getUseDuration", cancellable = true)
	public void getUseDuration(ItemStack stack, CallbackInfoReturnable<Integer> callbackInfo) {
		if (!FoodDrinks.eatingSpeedBasedOffFood || !Feature.isEnabled(FoodDrinks.class))
			return;

		if (stack.getItem().getFoodProperties() != null)
			callbackInfo.setReturnValue(FoodDrinks.getFoodConsumingTime(stack));
	}

	@Inject(at = @At("RETURN"), method = "getUseAnimation", cancellable = true)
	public void getUseAnimation(ItemStack stack, CallbackInfoReturnable<UseAnim> callbackInfo) {
		//Fix for SOL Valheim that calls getUseAnimation too early
		if (Feature.get(UnbreakableItems.class) == null)
			return;
		if (!Feature.isEnabled(FoodDrinks.class))
			return;

		if (stack.getItem() instanceof BowlFoodItem) {
			callbackInfo.setReturnValue(UseAnim.DRINK);
		}
	}

	@ModifyReturnValue(method = "getBarWidth", at = @At("RETURN"))
	public int iguanatweaksreborn$onGetBarWidth(int original, ItemStack stack) {
		if (!Feature.isEnabled(UnbreakableItems.class)
				|| !UnbreakableItems.isBroken(stack))
			return original;

		return 13;
	}

	@ModifyReturnValue(method = "getBarColor", at = @At("RETURN"))
	public int iguanatweaksreborn$onGetBarColor(int original, ItemStack stack) {
		if (!Feature.isEnabled(UnbreakableItems.class)
				|| !UnbreakableItems.isBroken(stack))
			return original;

		return 16711680;
	}

	@ModifyExpressionValue(method = "getBarWidth", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getMaxDamage(Lnet/minecraft/world/item/ItemStack;)I"))
	public int iguanatweaksreborn$onGetBarWidthMaxDamage(int original, ItemStack stack) {
		return stack.getMaxDamage();
	}

	@ModifyExpressionValue(method = "getBarColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getMaxDamage(Lnet/minecraft/world/item/ItemStack;)I"))
	public int iguanatweaksreborn$onGetBarColorMaxDamage(int original, ItemStack stack) {
		return stack.getMaxDamage();
	}
}
