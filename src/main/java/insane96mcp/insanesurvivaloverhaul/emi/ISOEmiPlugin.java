package insane96mcp.insanesurvivaloverhaul.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.Bows;
import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.FletchingFeature;
import insane96mcp.insanesurvivaloverhaul.module.farming.bonemeal.BoneMeal;
import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import insane96mcp.insanesurvivaloverhaul.module.farming.hoes.Hoes;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.insanesurvivaloverhaul.module.items.EcologicWood;
import insane96mcp.insanesurvivaloverhaul.module.items.NameTags;
import insane96mcp.insanesurvivaloverhaul.module.items.dagger.DaggerEquipment;
import insane96mcp.insanesurvivaloverhaul.module.items.repairkit.RepairKitRepairRecipe;
import insane96mcp.insanesurvivaloverhaul.module.items.repairkit.RepairKits;
import insane96mcp.insanesurvivaloverhaul.module.items.stonetoolsgone.StoneToolsGoneCommon;
import insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting.AnvilCrafting;
import insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting.AnvilRecipeReloadListener;
import insane96mcp.insanesurvivaloverhaul.module.misc.Conduit;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.module.misc.beacon.BeaconFeature;
import insane96mcp.insanesurvivaloverhaul.module.misc.lowfish.LowFish;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import insane96mcp.insanesurvivaloverhaul.module.mobs.spawning.Spawning;
import insane96mcp.insanesurvivaloverhaul.module.movement.minecarts.Minecarts;
import insane96mcp.insanesurvivaloverhaul.module.sleep.tiredness.Tiredness;
import insane96mcp.insanesurvivaloverhaul.module.world.Berries;
import insane96mcp.insanesurvivaloverhaul.module.world.Nether;
import insane96mcp.insanesurvivaloverhaul.module.world.Sponges;
import insane96mcp.insanesurvivaloverhaul.module.world.coalfire.CoalFire;
import insane96mcp.insanesurvivaloverhaul.module.world.spawners.Spawners;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
		if (Feature.isEnabled(FletchingFeature.class)) {
			registry.addCategory(EmiFletchingRecipe.CATEGORY);
			registry.addWorkstation(EmiFletchingRecipe.CATEGORY, EmiStack.of(Items.FLETCHING_TABLE));
			for (var recipe : registry.getRecipeManager().getAllRecipesFor(FletchingFeature.FLETCHING_RECIPE_TYPE.get())) {
				registry.addRecipe(new EmiFletchingRecipe(recipe.id(), recipe.value()));
			}
		}
		if (Feature.isEnabled(RepairKits.class)) {
			// Every repair kit variant is the same Item with different components (material/color); without this,
			// looking up "how to craft" any one variant shows every crafting recipe for repair_kit, since EMI's
			// default recipe lookup only keys by item type.
			registry.setDefaultComparison(RepairKits.ITEM.get(), Comparison.compareComponents());
			addRepairKitRecipes(registry);
		}
		addInfos(registry);
	}

	/**
	 * Registers info pages about ISO's new blocks/items and changes to vanilla ones. Each info is only added when the
	 * feature (and config option) it describes is active. Texts are kept generic (no values) to not spoil discovery,
	 * and things already explained by recipes, tooltips or advancements are left out.
	 */
	private void addInfos(EmiRegistry registry) {
		if (Feature.isEnabled(Spawning.class))
			addInfo(registry, emiIngredientOf(Spawning.ECHO_LANTERN.item().get()), "echo_lantern", "echo_lantern");
		if (Feature.isEnabled(DaggerEquipment.class))
			addInfo(registry, emiIngredientOf(DaggerEquipment.WOODEN_DAGGER.get(), DaggerEquipment.STONE_DAGGER.get(), DaggerEquipment.COPPER_DAGGER.get(), DaggerEquipment.IRON_DAGGER.get(), DaggerEquipment.GOLDEN_DAGGER.get(), DaggerEquipment.DIAMOND_DAGGER.get(), DaggerEquipment.NETHERITE_DAGGER.get()), "daggers", "daggers");
		if (Feature.isEnabled(Bows.class))
			addInfo(registry, emiIngredientOf(Bows.SHORTBOW.get()), "shortbow", "shortbow");
		if (Feature.isEnabled(BeaconFeature.class))
			addInfo(registry, emiIngredientOf(BeaconFeature.BEACON.item().get()), "beacon", "beacon");
		if (Feature.isEnabled(Tiredness.class) && !Tiredness.allowSleepingEvenWhenNotTired)
			addInfo(registry, EmiIngredient.of(ItemTags.BEDS), "beds", "beds");
		if (Feature.isEnabled(Hoes.class))
			addInfo(registry, EmiIngredient.of(ItemTags.HOES), "hoes", "hoes");
		if (Feature.isEnabled(EcologicWood.class))
			addInfo(registry, EmiIngredient.of(List.of(EmiIngredient.of(ISOItemTagsProvider.WOODEN_HAND_EQUIPMENT), EmiIngredient.of(EcologicWood.WOODEN_SHIELD))), "wooden_equipment", "wooden_equipment");
		if (Feature.isEnabled(StoneToolsGoneCommon.class) && StoneToolsGoneCommon.dataPack && !Packs.disableAllDataPacks)
			addInfo(registry, emiIngredientOf(Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE), "copper_ore", "copper_ore");
		if (Feature.isEnabled(Conduit.class))
			addInfo(registry, emiIngredientOf(Items.CONDUIT), "conduit", "conduit", Conduit.removeHaste ? "conduit.no_haste" : null);
		if (Feature.isEnabled(LowFish.class))
			addInfo(registry, emiIngredientOf(Items.FISHING_ROD), "fishing_rod", "fishing_rod");
		if (Feature.isEnabled(Berries.class) && Berries.noDamageIfDressed)
			addInfo(registry, emiIngredientOf(Items.SWEET_BERRIES), "sweet_berry_bush", "sweet_berry_bush");
		if (Feature.isEnabled(FoodDrinks.class) && FoodDrinks.buffCake)
			addInfo(registry, emiIngredientOf(Items.CAKE), "cake", "cake");
		if (Feature.isEnabled(Sponges.class))
			addInfo(registry, emiIngredientOf(Items.SPONGE, Items.WET_SPONGE), "sponge", "sponge", Sponges.dryWetWeather ? "sponge.weather" : null);
		if (Feature.isEnabled(CoalFire.class))
			addInfo(registry, emiIngredientOf(Items.CAMPFIRE, Items.SOUL_CAMPFIRE), "campfire",
					CoalFire.unlitCampfires ? "campfire.unlit" : null,
					CoalFire.campfireTurnOffUnderRain ? "campfire.rain" : null);
		if (Feature.isEnabled(Spawners.class))
			addInfo(registry, emiIngredientOf(Items.SPAWNER), "spawner",
					Spawners.ignoreLight ? "spawner.ignore_light" : null,
					Spawners.disableSpawners$enabled ? "spawner.disable" : null,
					Spawners.disableSpawners$enabled && Spawners.reEnableWithSpawnerReactivator ? "spawner.reactivate" : null);
		if (Feature.isEnabled(BoneMeal.class)) {
			addInfo(registry, emiIngredientOf(Items.BONE_MEAL), "bone_meal",
					BoneMeal.richFarmland$enable ? "bone_meal.rich_farmland" : null,
					BoneMeal.richFarmland$enable && BoneMeal.richFarmland$disableBoneMeal ? "bone_meal.no_crops" : null,
					BoneMeal.boneMealDirtToGrass ? "bone_meal.dirt_to_grass" : null,
					BoneMeal.boneMealCanesAndCactus.max > 0 ? "bone_meal.canes_and_cactus" : null);
			if (BoneMeal.richFarmland$enable)
				addInfo(registry, emiIngredientOf(BoneMeal.RICH_FARMLAND.item().get()), "rich_farmland", "bone_meal.rich_farmland", "rich_farmland");
		}
		if (Feature.isEnabled(Tweaks.class)) {
			if (Tweaks.fallingBreakingGlass) {
				List<EmiIngredient> breakOnFall = new ArrayList<>();
				BuiltInRegistries.BLOCK.getTagOrEmpty(Tweaks.BREAK_ON_FALL).forEach(holder -> {
					Item item = holder.value().asItem();
					if (item != Items.AIR)
						breakOnFall.add(EmiStack.of(item));
				});
				if (!breakOnFall.isEmpty())
					addInfo(registry, EmiIngredient.of(breakOnFall), "break_on_fall", "break_on_fall");
			}
			if (Tweaks.totemResistance)
				addInfo(registry, emiIngredientOf(Items.TOTEM_OF_UNDYING), "totem_of_undying", "totem_of_undying");
		}
	}

	/**
	 * Adds an info page with a line for each non-null lang key (relative to "emi.info.insanesurvivaloverhaul."). Nothing is added if every key is null.
	 */
	private void addInfo(EmiRegistry registry, EmiIngredient ingredient, String id, @Nullable String... langKeys) {
		List<Component> lines = new ArrayList<>();
		for (String langKey : langKeys) {
			if (langKey != null)
				lines.add(Component.translatable("emi.info.insanesurvivaloverhaul." + langKey));
		}
		if (lines.isEmpty())
			return;
		registry.addRecipe(new EmiInfoRecipe(List.of(ingredient), lines, InsaneSO.id("/info/" + id)));
	}

	/**
	 * Repair Kit repairs are a special crafting recipe, which EMI can't display. For every repair kit obtainable via
	 * crafting, shows a crafting recipe for each item it can repair: the item at 0 durability plus as many kits as
	 * needed to fully repair it (or as much as kits can). The result is computed by the actual recipe, so it reflects
	 * config and integrations.
	 */
	private void addRepairKitRecipes(EmiRegistry registry) {
		Level level = Minecraft.getInstance().level;
		if (level == null)
			return;
		RegistryAccess registryAccess = level.registryAccess();
		RepairKitRepairRecipe repairRecipe = new RepairKitRepairRecipe(CraftingBookCategory.EQUIPMENT);

		// Collect one kit per material from the crafting recipes producing a repair kit
		Map<String, ItemStack> kits = new LinkedHashMap<>();
		for (RecipeHolder<CraftingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
			ItemStack result = holder.value().getResultItem(registryAccess);
			if (!result.is(RepairKits.ITEM.get()))
				continue;
			String material = result.get(ISORegistries.REPAIR_KIT_MATERIAL.get());
			if (material != null)
				kits.putIfAbsent(material, result.copyWithCount(1));
		}

		for (ItemStack kit : kits.values()) {
			for (Item item : BuiltInRegistries.ITEM) {
				ItemStack damaged = new ItemStack(item);
				if (!damaged.isDamageableItem())
					continue;
				damaged.setDamageValue(damaged.getMaxDamage());
				// Add kits until the item is fully repaired, an extra kit doesn't repair any further (e.g. Max repair < 1) or the grid is full
				ItemStack repaired = ItemStack.EMPTY;
				int kitsUsed = 0;
				for (int kitAmount = 1; kitAmount <= 8; kitAmount++) {
					List<ItemStack> grid = new ArrayList<>(9);
					grid.add(damaged);
					for (int i = 0; i < 8; i++)
						grid.add(i < kitAmount ? kit : ItemStack.EMPTY);
					CraftingInput input = CraftingInput.of(3, 3, grid);
					if (!repairRecipe.matches(input, level))
						break;
					ItemStack result = repairRecipe.assemble(input, registryAccess);
					if (result.isEmpty() || (!repaired.isEmpty() && result.getDamageValue() >= repaired.getDamageValue()))
						break;
					repaired = result;
					kitsUsed = kitAmount;
					if (repaired.getDamageValue() == 0)
						break;
				}
				if (repaired.isEmpty())
					continue;
				List<EmiIngredient> inputs = new ArrayList<>();
				inputs.add(EmiStack.of(damaged));
				for (int i = 0; i < kitsUsed; i++)
					inputs.add(EmiStack.of(kit));
				ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
				String material = kit.get(ISORegistries.REPAIR_KIT_MATERIAL.get()).replace("#", "").replace(':', '/');
				registry.addRecipe(new EmiCraftingRecipe(inputs, EmiStack.of(repaired),
						InsaneSO.id("/repair_kit/%s/%s/%s".formatted(material, itemId.getNamespace(), itemId.getPath())), true));
			}
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
