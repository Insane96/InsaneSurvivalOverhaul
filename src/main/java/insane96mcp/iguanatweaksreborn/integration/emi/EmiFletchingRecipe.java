package insane96mcp.iguanatweaksreborn.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.crafting.FletchingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EmiFletchingRecipe implements EmiRecipe {
	private final ResourceLocation id;
	private final EmiIngredient ingredient;
	private final EmiIngredient catalyst1;
	private EmiIngredient catalyst2 = EmiIngredient.of(Ingredient.EMPTY);
	private final EmiStack output;

	public EmiFletchingRecipe(FletchingRecipe recipe) {
		this.id = recipe.getId();
		this.ingredient = EmiIngredient.of(Ingredient.of(recipe.getBaseIngredient()), recipe.getBaseIngredient().getCount());
		this.catalyst1 = EmiIngredient.of(Ingredient.of(recipe.getCatalyst1()), recipe.getCatalyst1().getCount());
		if (recipe.getCatalyst2() != null)
			this.catalyst2 = EmiIngredient.of(Ingredient.of(recipe.getCatalyst2()), recipe.getCatalyst2().getCount());
		this.output = EmiStack.of(recipe.getResult());
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return ISOEmiPlugin.FLETCHING_RECIPE_CATEGORY;
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
		widgets.addSlot(this.ingredient, 11, 20);
		widgets.addSlot(this.catalyst1, 2, 2);
		widgets.addSlot(this.catalyst2, 20, 2);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 40, 11);
		widgets.addSlot(this.output, 68, 7).large(true).recipeContext(this);
		//widgets.addText(Component.literal("" + this.smashesRequired), 32, 11, 0xFFFFFF, true);
		//widgets.addTooltipText(List.of(Component.translatable(IguanaTweaksExpanded.MOD_ID + ".smashes_required")), 30, 9, 18, 11);
		//widgets.addTexture(ForgeScreen.TEXTURE, 0, 0, 86, 58, 53, 14);
	}
}
