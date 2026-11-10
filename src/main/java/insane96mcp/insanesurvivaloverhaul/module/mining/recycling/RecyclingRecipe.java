package insane96mcp.insanesurvivaloverhaul.module.mining.recycling;

import insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting.RuneEnchantingIntegration;
import insane96mcp.insanesurvivaloverhaul.setup.ModIds;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

/**
 * Extends BlastingRecipe (rather than just AbstractCookingRecipe) so that recipe viewers like EMI,
 * which look up blast furnace recipes typed as RecipeType&lt;BlastingRecipe&gt;, recognize and display it.
 */
public class RecyclingRecipe extends BlastingRecipe {
	public RecyclingRecipe(String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
		super(group, category, ingredient, result, experience, cookingTime);
	}

	/**
	 * Enchanted items (and runed items, if Rune Enchanting is installed) can't be recycled unless {@link Recycling#allowEnchantedItems} is enabled.
	 */
	@Override
	public boolean matches(SingleRecipeInput input, Level level) {
		ItemStack in = input.item();
		if (!Recycling.allowEnchantedItems && (in.isEnchanted() || (ModList.get().isLoaded(ModIds.RUNE_ENCHANTING) && RuneEnchantingIntegration.hasRunes(in))))
			return false;
		return super.matches(input, level);
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
		return this.result.copyWithCount(this.getOutputCount(remainingDurability));
	}

	/**
	 * Returns the output of an undamaged item, with {@link Recycling#outputMultiplier} applied, so recipe viewers show the actual maximum output.
	 */
	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		if (this.result.isEmpty())
			return this.result;
		return this.result.copyWithCount(this.getOutputCount(1f));
	}

	private int getOutputCount(float remainingDurability) {
		int count = Mth.floor(this.result.getCount() * remainingDurability * Recycling.outputMultiplier);
		return Math.max(Recycling.minimumOutput, count);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Recycling.SERIALIZER.get();
	}
}
