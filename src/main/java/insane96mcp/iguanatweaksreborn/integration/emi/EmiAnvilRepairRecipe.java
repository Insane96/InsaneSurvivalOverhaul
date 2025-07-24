package insane96mcp.iguanatweaksreborn.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EmiAnvilRepairRecipe implements EmiRecipe {
	private final ResourceLocation id;
	private final EmiStack itemToRepair;
	private final EmiIngredient resource;
	private final EmiStack output;

	public EmiAnvilRepairRecipe(ResourceLocation id, ItemStack itemToRepair, Ingredient material, float materialAmount, float maxRepair) {
		this.id = id;
		ItemStack item = itemToRepair.copy();
		item.setDamageValue(itemToRepair.getMaxDamage() - 1);
		this.itemToRepair = EmiStack.of(item);
		this.resource = EmiIngredient.of(material, 1);
		item = itemToRepair.copy();
		item.setDamageValue(item.getMaxDamage() - (int) (item.getMaxDamage() / materialAmount));
		this.output = EmiStack.of(item);
	}

	public EmiAnvilRepairRecipe(ResourceLocation id, ItemStack itemToRepair, Item material, float materialAmount, float maxRepair) {
		this(id, itemToRepair, Ingredient.of(material), materialAmount, maxRepair);
	}

	public EmiAnvilRepairRecipe(ResourceLocation id, ItemStack itemToRepair, TagKey<Item> material, float materialAmount, float maxRepair) {
		this(id, itemToRepair, Ingredient.of(material), materialAmount, maxRepair);
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.ANVIL_REPAIRING;
	}

	@Override
	public @Nullable ResourceLocation getId() {
		return this.id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(this.itemToRepair, this.resource);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(this.output);
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}

	@Override
	public int getDisplayWidth() {
		return 125;
	}

	@Override
	public int getDisplayHeight() {
		return 18;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.PLUS, 27, 3);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, 1);
		widgets.addSlot(this.itemToRepair,0, 0);
		widgets.addSlot(this.resource, 49, 0);
		widgets.addSlot(this.output, 107, 0).recipeContext(this);
	}
}
