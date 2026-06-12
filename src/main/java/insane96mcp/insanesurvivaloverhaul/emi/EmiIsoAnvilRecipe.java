package insane96mcp.insanesurvivaloverhaul.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting.AnvilRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.List;

public class EmiIsoAnvilRecipe extends BasicEmiRecipe {
	public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
			ResourceLocation.fromNamespaceAndPath(InsaneSO.MOD_ID, "anvil"),
			EmiStack.of(Items.ANVIL)
	);

	public EmiIsoAnvilRecipe(AnvilRecipe recipe) {
		super(CATEGORY,
				ResourceLocation.fromNamespaceAndPath(InsaneSO.MOD_ID, "/anvil/" + BuiltInRegistries.ITEM.getKey(recipe.result.getItem()).getPath()),
				125, 18);
		this.inputs.addAll(List.of(
				EmiIngredient.of(recipe.leftIngredient.ingredient(), recipe.leftIngredient.count()),
				EmiIngredient.of(recipe.rightIngredient.ingredient(), recipe.rightIngredient.count())
		));
		this.outputs.add(EmiStack.of(recipe.result));
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.PLUS, 27, 3);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, 1);
		widgets.addSlot(inputs.get(0), 0, 0);
		widgets.addSlot(inputs.get(1), 49, 0);
		widgets.addSlot(outputs.get(0), 107, 0).recipeContext(this);
	}
}
