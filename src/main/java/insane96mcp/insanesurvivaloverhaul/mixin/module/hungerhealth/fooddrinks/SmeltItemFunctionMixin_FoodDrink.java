package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth.fooddrinks;

import com.mojang.logging.LogUtils;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SmeltItemFunction.class)
public class SmeltItemFunctionMixin_FoodDrink {

    @Final
    @Shadow
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void onRecipeRun(ItemStack stack, LootContext lootContext, CallbackInfoReturnable<ItemStack> cir) {
        if (!FoodDrinks.noFurnaceFoodAndSmokerRecipe)
            return;

        if (stack.isEmpty()) {
            cir.setReturnValue(stack);
        }
        else {
            Optional<RecipeHolder<CampfireCookingRecipe>> optional = lootContext.getLevel().getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(stack), lootContext.getLevel());
            if (optional.isPresent()) {
                ItemStack itemstack = optional.get().value().getResultItem(lootContext.getLevel().registryAccess());
                if (!itemstack.isEmpty()) {
                    cir.setReturnValue(itemstack.copyWithCount(stack.getCount() * itemstack.getCount())); // Forge: Support smelting returning multiple
                    return;
                }
            }

            LOGGER.warn("Couldn't smelt {} because there is no campfire cooking recipe", stack);
            cir.setReturnValue(stack);
        }

    }
}
