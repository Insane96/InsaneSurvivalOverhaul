package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth.fooddrinks;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoneyBottleItem.class)
public class HoneyBottleItemMixin_FoodDrinks {
    @Inject(at = @At("RETURN"), method = "getUseDuration", cancellable = true)
    public void getUseDuration(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (!Feature.isEnabled(FoodDrinks.class)
                || FoodDrinks.eatingSpeedFormula.isEmpty())
            return;
        cir.setReturnValue(FoodDrinks.getFoodConsumingTime(stack));
    }
}
