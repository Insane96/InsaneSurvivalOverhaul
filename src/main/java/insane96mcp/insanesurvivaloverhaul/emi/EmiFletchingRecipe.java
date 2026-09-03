package insane96mcp.insanesurvivaloverhaul.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.FletchingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EmiFletchingRecipe implements EmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(InsaneSO.MOD_ID, "fletching"),
            EmiStack.of(Items.FLETCHING_TABLE)
    );

    private final ResourceLocation id;
    private final EmiIngredient ingredient;
    private final EmiIngredient catalyst1;
    private final EmiIngredient catalyst2;
    private final EmiStack output;

    public EmiFletchingRecipe(ResourceLocation id, FletchingRecipe recipe) {
        this.id = id;
        this.ingredient = emiOf(recipe.getIngredient());
        this.catalyst1 = emiOf(recipe.getCatalyst1());
        this.catalyst2 = recipe.getCatalyst2().map(EmiFletchingRecipe::emiOf).orElse(EmiIngredient.of(Ingredient.EMPTY));
        this.output = EmiStack.of(recipe.getResult());
    }

    private static EmiIngredient emiOf(SizedIngredient sizedIngredient) {
        return EmiIngredient.of(sizedIngredient.ingredient(), sizedIngredient.count());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return this.id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(this.ingredient, this.catalyst1, this.catalyst2);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(this.output);
    }

    @Override
    public int getDisplayWidth() {
        return 96;
    }

    @Override
    public int getDisplayHeight() {
        return 42;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(this.catalyst1, 2, 2);
        widgets.addSlot(this.catalyst2, 20, 2);
        widgets.addSlot(this.ingredient, 11, 20);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 40, 11);
        widgets.addSlot(this.output, 68, 7).large(true).recipeContext(this);
    }
}
