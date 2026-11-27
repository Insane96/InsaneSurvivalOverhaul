package insane96mcp.insanesurvivaloverhaul.mixin.module.item.enchantedrestrictions;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanesurvivaloverhaul.module.items.EnchantedItemsRestrictions;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShapelessRecipe.class)
public class ShapelessRecipeMixin_EnchantedItemsRestrictions {
	/**
	 * Shapeless recipes no longer match if any of the ingredients is enchanted (or has runes), so enchanted items can't be wasted in crafting.
	 */
	@ModifyReturnValue(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z", at = @At("RETURN"))
	private boolean insanesurvivaloverhaul$noEnchantedIngredients(boolean original, @Local(argsOnly = true) CraftingInput input) {
		return original && !EnchantedItemsRestrictions.preventsCrafting(input);
	}
}
