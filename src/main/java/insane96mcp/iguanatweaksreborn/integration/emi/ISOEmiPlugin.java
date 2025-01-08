package insane96mcp.iguanatweaksreborn.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.experience.anvils.AnvilRepair;
import insane96mcp.iguanatweaksreborn.module.experience.anvils.AnvilRepairReloadListener;
import insane96mcp.iguanatweaksreborn.module.experience.anvils.Anvils;
import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.iguanatweaksreborn.module.items.NameTags;
import insane96mcp.iguanatweaksreborn.module.movement.minecarts.Minecarts;
import insane96mcp.iguanatweaksreborn.module.world.coalfire.CoalFire;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;

@EmiEntrypoint
public class ISOEmiPlugin implements EmiPlugin {

	@Override
	public void register(EmiRegistry registry) {
		RecipeManager manager = registry.getRecipeManager();

		if (Feature.isEnabled(Anvils.class)) {
			for (Map.Entry<ResourceLocation, AnvilRepair> anvilRepair : AnvilRepairReloadListener.REPAIRS.entrySet()) {
				for (ItemStack stack : anvilRepair.getValue().itemToRepair.getAllItemStacks()) {
					for (AnvilRepair.RepairData repairData : anvilRepair.getValue().repairData) {
						IdTagMatcher idTagMatcher = repairData.repairMaterial();
						if (idTagMatcher.type == IdTagMatcher.Type.ID)
							registry.addRecipe(new EmiAnvilRepairRecipe(ResourceLocation.tryParse(anvilRepair.getKey() + "_id_" + idTagMatcher.location.getPath()), stack, ForgeRegistries.ITEMS.getValue(idTagMatcher.location), repairData.amountRequired(), repairData.maxRepair()));
						else
							registry.addRecipe(new EmiAnvilRepairRecipe(ResourceLocation.tryParse(anvilRepair.getKey() + "_tag_" + idTagMatcher.location.getPath()), stack, TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), idTagMatcher.location), repairData.amountRequired(), repairData.maxRepair()));

					}
				}
			}
			registry.removeRecipes(recipe -> recipe.getId() != null && "emi".equals(recipe.getId().getNamespace()) && recipe.getId().getPath().startsWith("/anvil/repairing/material"));
			registry.removeRecipes(recipe -> recipe.getId() != null && "emi".equals(recipe.getId().getNamespace()) && recipe.getId().getPath().startsWith("/anvil/enchanting"));
		}
		if (Feature.isEnabled(NameTags.class)) {
			registry.addRecipe(createSimpleInfo(Items.NAME_TAG, "name_tag", Component.translatable("emi.info.iguanatweaksreborn.items.name_tags")));
		}
		if (Feature.isEnabled(Crops.class)) {
			registry.addRecipe(createSimpleInfo(emiIngredientOf(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Crops.CARROT_SEEDS.get(), Crops.ROOTED_POTATO.get()), "crops", Component.translatable("emi.info.iguanatweaksreborn.crops.seeds")));
		}
		if (Feature.isEnabled(Minecarts.class)) {
			registry.addRecipe(createSimpleInfo(Minecarts.COPPER_POWERED_RAIL.item().get(), "info_copper_powered_rail", Component.translatable("emi.info.iguanatweaksreborn.copper_powered_rail")));
			registry.addRecipe(createSimpleInfo(Minecarts.GOLDEN_POWERED_RAIL.item().get(), "info_golden_powered_rail", Component.translatable("emi.info.iguanatweaksreborn.golden_powered_rail")));
			registry.removeEmiStacks(emiStack -> emiStack.getItemStack().is(Items.POWERED_RAIL));
		}
		if (Feature.isEnabled(CoalFire.class) && CoalFire.charcoalFromBurntLogsChance > 0) {
			Ingredient fire = Ingredient.of(CoalFire.FIRESTARTER.get(), Items.FLINT_AND_STEEL);
			registry.addRecipe(EmiWorldInteractionRecipe.builder()
					.id(new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "charcoal_from_burning_logs"))
					.leftInput(EmiIngredient.of(ItemTags.LOGS_THAT_BURN))
					.rightInput(EmiIngredient.of(fire), false, slotWidget -> slotWidget.appendTooltip(Component.literal("Basically fire").withStyle(ChatFormatting.GREEN)))
					.output(EmiStack.of(Items.CHARCOAL)).build());
			registry.addRecipe(EmiWorldInteractionRecipe.builder()
					.id(new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "charcoal_layer_from_burning_logs"))
					.leftInput(EmiIngredient.of(ItemTags.LOGS_THAT_BURN))
					.rightInput(EmiIngredient.of(fire), false, slotWidget -> slotWidget.appendTooltip(Component.literal("Basically fire").withStyle(ChatFormatting.GREEN)))
					.output(EmiStack.of(CoalFire.CHARCOAL_LAYER.item().get())).build());
		}
	}

	public EmiInfoRecipe createSimpleInfo(Item item, String id, Component component) {
		return new EmiInfoRecipe(List.of(emiIngredientOf(item)), List.of(component), new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, id));
	}

	public EmiInfoRecipe createSimpleInfo(EmiIngredient emiIngredient, String id, Component component) {
		return new EmiInfoRecipe(List.of(emiIngredient), List.of(component), new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, id));
	}

	public static EmiIngredient emiIngredientOf(Item... item) {
		return EmiIngredient.of(Ingredient.of(item));
	}
}
