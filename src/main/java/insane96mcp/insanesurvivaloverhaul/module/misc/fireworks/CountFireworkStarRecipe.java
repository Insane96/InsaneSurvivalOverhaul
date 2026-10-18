package insane96mcp.insanesurvivaloverhaul.module.misc.fireworks;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Vanilla Firework Star recipe with a configurable output count
 */
public class CountFireworkStarRecipe extends FireworkStarRecipe {
    private final int count;

    public CountFireworkStarRecipe(CraftingBookCategory category, int count) {
        super(category);
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack stack = super.assemble(input, registries);
        stack.setCount(this.count);
        return stack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FireworksFeature.FIREWORK_STAR_SERIALIZER.get();
    }
}
