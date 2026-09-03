package insane96mcp.insanesurvivaloverhaul.module.combat.fletching;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.Optional;

public class FletchingRecipe implements Recipe<FletchingRecipeInput> {
    private final SizedIngredient ingredient;
    private final SizedIngredient catalyst1;
    private final Optional<SizedIngredient> catalyst2;
    private final ItemStack result;

    public FletchingRecipe(SizedIngredient ingredient, SizedIngredient catalyst1, Optional<SizedIngredient> catalyst2, ItemStack result) {
        this.ingredient = ingredient;
        this.catalyst1 = catalyst1;
        this.catalyst2 = catalyst2;
        this.result = result;
    }

    @Override
    public boolean matches(FletchingRecipeInput input, Level level) {
        return this.ingredient.test(input.ingredient())
                && this.catalyst1.test(input.catalyst1())
                && this.catalyst2.map(sized -> sized.test(input.catalyst2())).orElse(true);
    }

    @Override
    public ItemStack assemble(FletchingRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(this.ingredient.ingredient());
        list.add(this.catalyst1.ingredient());
        this.catalyst2.ifPresent(sized -> list.add(sized.ingredient()));
        return list;
    }

    public SizedIngredient getIngredient() {
        return this.ingredient;
    }

    public SizedIngredient getCatalyst1() {
        return this.catalyst1;
    }

    public Optional<SizedIngredient> getCatalyst2() {
        return this.catalyst2;
    }

    public ItemStack getResult() {
        return this.result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FletchingFeature.FLETCHING_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return FletchingFeature.FLETCHING_RECIPE_TYPE.get();
    }
}
