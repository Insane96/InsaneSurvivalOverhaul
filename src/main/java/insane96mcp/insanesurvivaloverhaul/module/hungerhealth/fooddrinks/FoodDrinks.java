package insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks;

import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.event.CakeEatEvent;
import insane96mcp.insanelib.module.base.items.ItemComponentsReloadListener;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Map;

@LoadFeature(module = ISOModules.HUNGER_HEALTH, name = "Foods & Drinks", description = "Changes to food nourishment and the speed on how food is eaten or how items are consumed. Custom Food Properties are controlled via json in this feature's folder.")
public class FoodDrinks extends JsonFeature {
	public static final ResourceKey<LootTable> PUMPKIN_SHEAR_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, InsaneSO.id("gameplay/pumpkin_shear"));

	private static final FoodProperties SINGLE_MUSHROOM_STEW = new FoodProperties.Builder().nutrition(3).saturationModifier(0.4F).usingConvertsTo(Items.BOWL).build();

	public static final DeferredHolder<Item, Item> BROWN_MUSHROOM_STEW = ISORegistries.ITEMS.register("brown_mushroom_stew", () -> new Item(new Item.Properties()
			.food(SINGLE_MUSHROOM_STEW)
	));
	public static final DeferredHolder<Item, Item> RED_MUSHROOM_STEW = ISORegistries.ITEMS.register("red_mushroom_stew", () -> new Item(new Item.Properties()
			.food(SINGLE_MUSHROOM_STEW)
	));
	public static final DeferredHolder<Item, Item> NETHERIZED_STEW = ISORegistries.ITEMS.register("netherized_stew", () -> new Item(new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(12).saturationModifier(1.2F).effect(() -> new MobEffectInstance(MobEffects.POISON, 30 * 20, 0), 0.8f).build())
	));

	public static final DeferredHolder<Item, Item> OVER_EASY_EGG = ISORegistries.ITEMS.register("over_easy_egg", () -> new Item(new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build())
	));

	public static final DeferredHolder<Item, Item> PUMPKIN_PULP = ISORegistries.ITEMS.register("pumpkin_pulp", () -> new Item(new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).build())
	));

	public static final TagKey<Item> RAW_FOODS = ISOItemTagsProvider.create("raw_foods");
	public static final TagKey<Item> DRINKING_FOODS = ISOItemTagsProvider.create("drinking_foods");

	@Config(description = "Food's hunger restored will be calculated from this formula. Variables as nutrition, saturation, eat_seconds as numbers and can_always_eat as boolean can be used. NOTE: this is calculated from the original food properties, not the modified ones with the other formulas. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html. Empty to disable. Can be re-applied with /reload")
	public static String foodHungerFormula = "ROUND(nutrition * 1.2, 0)";
	@Config(description = "Food's saturation multiplier will be calculated from this formula. This is not a flat value: https://minecraft.wiki/w/Hunger#Food_level_and_saturation_level_restoration. Variables as nutrition, saturation, eat_seconds as numbers and can_always_eat as boolean can be used. NOTE: this is calculated from the original food properties, not the modified ones with the other formulas. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html. Empty to disable. Can be re-applied with /reload")
	public static String foodSaturationFormula = "saturation * 1.4";
	@Config(description = "The formula to calculate the seconds required to eat a food. Variables as nutrition, saturation, eat_seconds as numbers and can_always_eat as boolean can be used. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html. NOTE: this is calculated from the original food properties, not the modified ones with the other formulas. Empty to disable. Can be re-applied with /reload.")
	public static String foodEatSecondsFormula = "MIN(MAX((1.6 * (nutrition + saturation)) * 0.125, 0.8), 5)";

	@Config(description = "Makes potions and milk faster to drink, 1 second instead of 1.6.")
	public static Boolean fasterDrinkConsuming = true;
	@Config(description = "If true, eating/drinking stops when the player's hit.")
	public static Boolean stopConsumingOnHit = true;
	@Config(description = "If true, eating will always be possible, even with full hunger.")
	public static Boolean alwaysEat = false;
	@Config(description = "If enabled, when eating food, the saturation will not sum, instead will just be set to the food's saturation (if higher than the current).")
	public static Boolean combatSnapshotEatingSaturation = true;
	@Config(description = "If enabled, eating cakes will give 30 seconds of Speed and Haste")
	public static Boolean buffCake = true;
    @Config(description = "Adds a loot table when shearing pumpkins (insanesurvivaloverhaul:gameplay/pumpkin_shear). This also replaces seeds drop with Pumpkin Pulp")
    public static Boolean addPumpkinShearLootTable = true;

	@Config(description = "Enables a data pack that makes food no longer smeltable in furnaces and changes smokers recipe to require soul sand.\nThis also enables a change to the smelt_item_function in loot tables to use smoker recipes instead of furnaces (otherwise, mobs wouldn't drop cooked food). Might have unintended side effects.")
	public static Boolean noFurnaceFoodAndSmokerRecipe = true;
	@Config(description = "Enables a data pack that rebalances some foods and makes raw foods poisonous.")
	public static Boolean foodChanges = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("no_food_in_furnace", "Insane's Survival Overhaul No Food in Furnace", () -> this.isEnabled() && !Packs.disableAllDataPacks && noFurnaceFoodAndSmokerRecipe);
		InsaneSO.addServerPack("food_changes", "Insane's Survival Overhaul Food Changes", () -> this.isEnabled() && !Packs.disableAllDataPacks && foodChanges);
		ItemComponentsReloadListener.PROGRAMMATIC_PROVIDERS.add((registryAccess, patches) -> {
			if (!this.isEnabled()) return;
			processFoodMultipliers(patches);
		});
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSO.CONFIG_FOLDER;
	}

	@Override
	public void loadJsonConfigs() {
		if (!this.isEnabled())
			return;
		super.loadJsonConfigs();
		//processFoodMultipliers(false);
	}

	@SubscribeEvent
	public void onCakeEat(CakeEatEvent event) {
		if (!this.isEnabled()
				|| !buffCake)
			return;

		LivingEntity entity = event.getEntity();
		entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, stackedCakeDuration(entity, MobEffects.MOVEMENT_SPEED), 0, false, false, true));
		entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, stackedCakeDuration(entity, MobEffects.DIG_SPEED), 0, false, false, true));
	}

	private static int stackedCakeDuration(LivingEntity entity, Holder<MobEffect> effect) {
		MobEffectInstance existing = entity.getEffect(effect);
		int current = existing != null ? existing.getDuration() : 0;
		// Diminishing returns: each slice adds less the closer to the cap (1200t = 60s)
		return Math.min(1200, (int) (current + 600 * (1.0 - (double) current / 2400)));
	}

	@SubscribeEvent
	public void onPlayerHit(LivingDamageEvent.Pre event) {
		if (!this.isEnabled()
				|| !stopConsumingOnHit
				|| !(event.getSource().getEntity() instanceof LivingEntity)
				|| !(event.getEntity() instanceof Player player)
				|| (!player.getUseItem().getUseAnimation().equals(UseAnim.EAT) && !player.getUseItem().getUseAnimation().equals(UseAnim.DRINK)))
			return;
		player.stopUsingItem();
	}

	private static void processFoodMultipliers(Map<Item, DataComponentPatch> patches) {
		if (foodHungerFormula.isBlank() && foodSaturationFormula.isBlank() && foodEatSecondsFormula.isBlank())
			return;

		for (Item item : BuiltInRegistries.ITEM) {
			FoodProperties food = item.components().get(DataComponents.FOOD);
			if (food == null)
				continue;

			int newNutrition = !foodHungerFormula.isBlank()
					? (int) MCUtils.computeFoodFormula(food, foodHungerFormula)
					: food.nutrition();
			float newSaturation = !foodSaturationFormula.isBlank()
					? MCUtils.computeFoodFormula(food, foodSaturationFormula)
					: food.saturation();
			float newEatSeconds = !foodEatSecondsFormula.isBlank()
					? MCUtils.computeFoodFormula(food, foodEatSecondsFormula)
					: food.eatSeconds();

			if (newNutrition == food.nutrition() && newSaturation == food.saturation() && newEatSeconds == food.eatSeconds())
				continue;

			FoodProperties modified = new FoodProperties(newNutrition, newSaturation, food.canAlwaysEat(), newEatSeconds, food.usingConvertsTo(), food.effects());
			patches.put(item, DataComponentPatch.builder().set(DataComponents.FOOD, modified).build());
		}
	}
}
