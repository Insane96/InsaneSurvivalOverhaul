package insane96mcp.insanesurvivaloverhaul.mixin.module.item.enchantedrestrictions;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.items.EnchantedItemsRestrictions;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StackedContents.class)
public class StackedContentsMixin_EnchantedItemsRestrictions {
	/**
	 * The recipe book already skips enchanted items when filling the crafting grid; this makes it skip items with runes too, as they can't be used in crafting.
	 */
	@ModifyExpressionValue(method = "accountSimpleStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEnchanted()Z"))
	private boolean insanesurvivaloverhaul$skipRunedItems(boolean original, @Local(argsOnly = true) ItemStack stack) {
		return original
				|| (EnchantedItemsRestrictions.noCrafting
					&& Feature.isEnabled(EnchantedItemsRestrictions.class)
					&& EnchantedItemsRestrictions.hasRunes(stack));
	}
}
