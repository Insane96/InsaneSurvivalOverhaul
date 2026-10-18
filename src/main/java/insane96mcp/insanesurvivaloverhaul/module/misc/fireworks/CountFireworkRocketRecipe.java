package insane96mcp.insanesurvivaloverhaul.module.misc.fireworks;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.FireworkRocketRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Vanilla Firework Rocket recipe with a configurable output count
 */
public class CountFireworkRocketRecipe extends FireworkRocketRecipe {
    private final int count;

    public CountFireworkRocketRecipe(CraftingBookCategory category, int count) {
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
        return FireworksFeature.FIREWORK_ROCKET_SERIALIZER.get();
    }
}
