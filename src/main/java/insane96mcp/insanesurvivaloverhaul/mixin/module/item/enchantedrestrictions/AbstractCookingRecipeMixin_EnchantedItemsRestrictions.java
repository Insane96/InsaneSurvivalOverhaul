package insane96mcp.insanesurvivaloverhaul.mixin.module.item.enchantedrestrictions;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanesurvivaloverhaul.module.items.EnchantedItemsRestrictions;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractCookingRecipe.class)
public class AbstractCookingRecipeMixin_EnchantedItemsRestrictions {
	/**
	 * Furnace, smoker, blast furnace and campfire recipes no longer match enchanted (or runed) items, e.g. enchanted golden swords can't be smelted into nuggets.
	 */
	@ModifyReturnValue(method = "matches(Lnet/minecraft/world/item/crafting/SingleRecipeInput;Lnet/minecraft/world/level/Level;)Z", at = @At("RETURN"))
	private boolean insanesurvivaloverhaul$noEnchantedSmelting(boolean original, @Local(argsOnly = true) SingleRecipeInput input) {
		return original && !EnchantedItemsRestrictions.preventsSmelting(input.item());
	}
}
