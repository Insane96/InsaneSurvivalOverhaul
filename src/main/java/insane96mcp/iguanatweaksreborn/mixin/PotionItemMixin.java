package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.iguanatweaksreborn.module.misc.PotionsAndEffects;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SplashPotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionItemMixin {
	@Inject(at = @At("RETURN"), method = "getUseDuration", cancellable = true)
	public void getUseDuration(ItemStack stack, CallbackInfoReturnable<Integer> callbackInfo) {
		if (!FoodDrinks.fasterDrinkConsuming || !Feature.isEnabled(FoodDrinks.class))
			return;

		callbackInfo.setReturnValue(20);
	}

    @ModifyExpressionValue(method = "appendHoverText", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
    public float iguanatweaksreborn$appendHoverText(float original) {
        return (Object) this instanceof SplashPotionItem && PotionsAndEffects.streamlineSplashPotions() ? PotionsAndEffects.STREAMLINE_SPLASH_POTION_MULTIPLIER : original;
    }
}
