package insane96mcp.insanesurvivaloverhaul.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import insane96mcp.insanesurvivaloverhaul.module.items.NameTags;
import insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting.AnvilCrafting;
import insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting.AnvilRecipeReloadListener;
import insane96mcp.insanesurvivaloverhaul.module.movement.minecarts.Minecarts;
import insane96mcp.insanesurvivaloverhaul.module.world.Nether;
import insane96mcp.insanesurvivaloverhaul.module.world.coalfire.CoalFire;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

@EmiEntrypoint
public class ISOEmiPlugin implements EmiPlugin {
	@Override
	public void register(EmiRegistry registry) {
		if (Feature.isEnabled(AnvilCrafting.class)) {
			registry.addCategory(EmiIsoAnvilRecipe.CATEGORY);
			registry.addWorkstation(EmiIsoAnvilRecipe.CATEGORY, EmiStack.of(Items.ANVIL));
			registry.addWorkstation(EmiIsoAnvilRecipe.CATEGORY, EmiStack.of(Items.CHIPPED_ANVIL));
			registry.addWorkstation(EmiIsoAnvilRecipe.CATEGORY, EmiStack.of(Items.DAMAGED_ANVIL));

			if (AnvilRecipeReloadListener.RECIPES != null) {
				for (var recipe : AnvilRecipeReloadListener.RECIPES) {
					registry.addRecipe(new EmiIsoAnvilRecipe(recipe));
				}
			}
		}
		if (Feature.isEnabled(NameTags.class)) {
			registry.addRecipe(createSimpleInfo(Items.NAME_TAG, "name_tag", Component.translatable("emi.info.insanesurvivaloverhaul.items.name_tags")));
		}
		if (Feature.isEnabled(Crops.class)) {
			registry.addRecipe(createSimpleInfo(emiIngredientOf(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Crops.CARROT_SEEDS.get(), Crops.ROOTED_POTATO.get()), "crops", Component.translatable("emi.info.insanesurvivaloverhaul.crops.seeds")));
		}
		if (Feature.isEnabled(Minecarts.class)) {
			registry.addRecipe(createSimpleInfo(Minecarts.COPPER_POWERED_RAIL.item().get(), "info_copper_powered_rail", Component.translatable("emi.info.insanesurvivaloverhaul.copper_powered_rail")));
			registry.addRecipe(createSimpleInfo(Minecarts.GOLDEN_POWERED_RAIL.item().get(), "info_golden_powered_rail", Component.translatable("emi.info.insanesurvivaloverhaul.golden_powered_rail")));
			registry.removeEmiStacks(emiStack -> emiStack.getItemStack().is(Items.POWERED_RAIL));
		}
		if (Feature.isEnabled(CoalFire.class) && CoalFire.burntLogsChance > 0) {
			Ingredient fire = Ingredient.of(Items.FLINT, CoalFire.FIRESTARTER.get(), Items.FLINT_AND_STEEL);
			registry.addRecipe(EmiWorldInteractionRecipe.builder()
					.id(InsaneSO.id("/burnt_log"))
					.leftInput(EmiIngredient.of(ItemTags.LOGS_THAT_BURN))
					.rightInput(EmiIngredient.of(fire), false, slotWidget -> slotWidget.appendTooltip(Component.literal("Basically fire").withStyle(ChatFormatting.GREEN)))
					.output(EmiStack.of(CoalFire.BURNT_LOG.item().get())).build());
		}
		if (Feature.isEnabled(Nether.class)) {
			if (Nether.portalRequiresCryingObsidian)
				registry.addRecipe(createSimpleInfo(EmiIngredient.of(Nether.PORTAL_CORNERS), "info_portal_corners", Component.translatable("emi.info.insanesurvivaloverhaul.portal_corners")));
		}
	}

	public EmiInfoRecipe createSimpleInfo(Item item, String id, Component component) {
		return new EmiInfoRecipe(List.of(emiIngredientOf(item)), List.of(component), InsaneSO.id("/" + id));
	}

	public EmiInfoRecipe createSimpleInfo(EmiIngredient emiIngredient, String id, Component component) {
		return new EmiInfoRecipe(List.of(emiIngredient), List.of(component), InsaneSO.id("/" + id));
	}

	public static EmiIngredient emiIngredientOf(Item... item) {
		return EmiIngredient.of(Ingredient.of(item));
	}
}
