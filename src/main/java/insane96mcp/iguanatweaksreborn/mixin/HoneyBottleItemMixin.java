package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoneyBottleItem.class)
public class HoneyBottleItemMixin {
    @Inject(at = @At("RETURN"), method = "getUseDuration", cancellable = true)
    public void getUseDuration(ItemStack stack, CallbackInfoReturnable<Integer> callbackInfo) {
        if (!Feature.isEnabled(FoodDrinks.class))
            return;
        if (!FoodDrinks.eatingSpeedFormula.isEmpty())
            callbackInfo.setReturnValue(FoodDrinks.getFoodConsumingTime(stack));
    }
}
