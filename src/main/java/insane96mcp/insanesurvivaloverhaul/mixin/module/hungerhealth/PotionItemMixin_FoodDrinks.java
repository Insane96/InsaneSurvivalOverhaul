package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.insanesurvivaloverhaul.module.misc.PotionsAndEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SplashPotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionItemMixin_FoodDrinks {
	@Inject(at = @At("RETURN"), method = "getUseDuration", cancellable = true)
	public void getUseDuration(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
		if (!FoodDrinks.fasterDrinkConsuming || !Feature.isEnabled(FoodDrinks.class))
			return;

		cir.setReturnValue(20);
	}

    @ModifyExpressionValue(method = "appendHoverText", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
    public float iguanatweaksreborn$appendHoverText(float original) {
        return (Object) this instanceof SplashPotionItem && PotionsAndEffects.streamlineSplashPotions() ? PotionsAndEffects.STREAMLINE_SPLASH_POTION_MULTIPLIER : original;
    }
}
