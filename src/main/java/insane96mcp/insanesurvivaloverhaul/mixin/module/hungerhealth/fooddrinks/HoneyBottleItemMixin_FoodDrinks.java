package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth.fooddrinks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HoneyBottleItem.class)
public abstract class HoneyBottleItemMixin_FoodDrinks extends Item {
    public HoneyBottleItemMixin_FoodDrinks(Properties properties) {
        super(properties);
    }

    @ModifyReturnValue(at = @At("RETURN"), method = "getUseDuration")
    public int getUseDuration(int original, ItemStack stack, LivingEntity entity) {
        if (!Feature.isEnabled(FoodDrinks.class))
            return original;
        return super.getUseDuration(stack, entity);
    }
}
