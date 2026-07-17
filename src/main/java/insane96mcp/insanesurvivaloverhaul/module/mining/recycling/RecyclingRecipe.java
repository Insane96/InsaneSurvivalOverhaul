package insane96mcp.insanesurvivaloverhaul.module.mining.recycling;

import net.minecraft.core.HolderLookup;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

/**
 * Extends BlastingRecipe (rather than just AbstractCookingRecipe) so that recipe viewers like EMI,
 * which look up blast furnace recipes typed as RecipeType&lt;BlastingRecipe&gt;, recognize and display it.
 */
public class RecyclingRecipe extends BlastingRecipe {
	public RecyclingRecipe(String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
		super(group, category, ingredient, result, experience, cookingTime);
	}

	/**
	 * The recipe's "result" ItemStack holds the output amount at full durability.
	 * The actual output is scaled down by how much of the item's durability has been consumed.
	 */
	@Override
	public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
		ItemStack in = input.item();
		if (this.result.isEmpty() || !in.isDamageableItem() || in.getMaxDamage() <= 0)
			return this.result.copy();

		float remainingDurability = (float) (in.getMaxDamage() - in.getDamageValue()) / in.getMaxDamage();
		int count = Mth.floor(this.result.getCount() * remainingDurability * Recycling.outputMultiplier);
		count = Math.max(Recycling.minimumOutput, count);
		return this.result.copyWithCount(count);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Recycling.SERIALIZER.get();
	}
}
