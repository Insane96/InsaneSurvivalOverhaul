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
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
@LoadFeature(module = ISOModules.HUNGER_HEALTH, name = "Foods & Drinks", description = "Changes to food nourishment and the speed on how food is eaten or how items are consumed. Custom Food Properties are controlled via json in this feature's folder.")
public class FoodDrinks extends JsonFeature {
	public static final ResourceKey<LootTable> PUMPKIN_SHEAR_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, InsaneSO.location("gameplay/pumpkin_shear"));

	private static final FoodProperties SINGLE_MUSHROOM_STEW = new FoodProperties.Builder().nutrition(3).saturationModifier(0.6F).usingConvertsTo(Items.BOWL).build();

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

	public static final TagKey<Item> RAW_FOOD = ISOItemTagsProvider.create("raw_food");
	public static final TagKey<Item> DRINKING_FOODS = ISOItemTagsProvider.create("drinking_foods");
	/*public static final TagKey<Item> FOOD_BLACKLIST = ISOItemTagsProvider.create("food_drinks_no_hunger_changes");

	//TODO
	public static final ArrayList<CustomFoodProperties> CUSTOM_FOOD_PROPERTIES_DEFAULT = new ArrayList<>(List.of(
            new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:chorus_fruit")).fastEating(true).build(),
            new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:rotten_flesh")).nutrition(2).setEatingTime(55).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:spider_eye")).nutrition(1).setEatingTime(40).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:honey_bottle")).nutrition(2).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:cooked_beef")).nutrition(6).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:cooked_porkchop")).nutrition(7).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:pumpkin_pie")).nutrition(6).setEatingTime(40).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:rabbit_stew")).nutrition(12).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:glow_berries")).alwaysEat(true).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:cookie")).nutrition(1).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:golden_apple"))
					.addEffect(new ISOMobEffectInstance.Builder(MobEffects.REGENERATION, 100).setAmplifier(1).build())
					.addEffect(new ISOMobEffectInstance.Builder(RegeneratingAbsorption.EFFECT, 2400).build()).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:enchanted_golden_apple"))
					.addEffect(new ISOMobEffectInstance.Builder(MobEffects.REGENERATION, 400).setAmplifier(1).build())
					.addEffect(new ISOMobEffectInstance.Builder(MobEffects.DAMAGE_RESISTANCE, 6000).build())
					.addEffect(new ISOMobEffectInstance.Builder(MobEffects.FIRE_RESISTANCE, 6000).build())
					.addEffect(new ISOMobEffectInstance.Builder(RegeneratingAbsorption.EFFECT, 2400).setAmplifier(3).build()).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("berry_good:sweet_berry_meatballs")).nutrition(9).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("berry_good:glowgurt")).nutrition(8).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("farmersdelight:bone_broth")).nutrition(6).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("autumnity:pumpkin_bread")).nutrition(5).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("buzzier_bees:honey_bread")).nutrition(5).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("farmersdelight:pumpkin_slice")).nutrition(2).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("tide:cooked_fish")).nutrition(5).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("environmental:plum")).nutrition(3).build()
	));
	public static final ArrayList<CustomFoodProperties> customFoodProperties = new ArrayList<>();*/

	@Config(description = "Food's hunger restored will be calculated from this formula. Variables as nutrition, saturation, eat_seconds as numbers and can_always_eat as boolean can be used. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html. Setting this to an empty string disables the feature. Can be re-applied with /reload")
	public static String foodHungerFormula = "";
	@Config(name = "Food Saturation Modifier Formula", description = "Food's saturation multiplier will be calculated from this formula. This is not a flat value: https://minecraft.wiki/w/Hunger#Food_level_and_saturation_level_restoration. Variables as nutrition, saturation, eat_seconds as numbers and can_always_eat as boolean can be used. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html. Setting this to an empty string disables the feature. Can be re-applied with /reload")
	public static String foodSaturationFormula = "saturation * 1.2";

	@Config(description = "Makes potions and, milk faster to drink, 1 second instead of 1.6.")
	public static Boolean fasterDrinkConsuming = true;
	@Config(description = "The formula to calculate the seconds required to eat a food. Variables as nutrition, saturation, eat_seconds as numbers and can_always_eat as boolean can be used. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html. The default formula increases the time to eat exponentially when higher effectiveness. Empty to disable")
	public static String eatingSpeedFormula = "MIN(MAX((32 * (nutrition + saturation)) * 0.075, 20), 75)";
	@Config(description = "If true, eating/drinking stops when the player's hit.")
	public static Boolean stopConsumingOnHit = true;
	@Config(description = "If true, eating will always be possible, even with full hunger.")
	public static Boolean alwaysEat = false;
	//TODO Item properties
	@Config(min = 0d, max = 1f, description = "Raw food has this chance to poison the player. Raw food is defined in the insanesurvivaloverhaul:raw_food tag")
	public static Double rawFoodPoisonChance = 0.7d;
	@Config(min = 0d, max = 255, description = "Raw food will give this level of poison to the player.")
	public static Integer rawFoodPoisonAmplifier = 1;
	@Config(min = 0d, description = "Raw food's poison duration will be multiplied by this value. With this set to 1, raw food will give 1 second of poison per nutrition + saturation given.")
	public static Double rawFoodPoisonDurationMultiplier = 2d;
	@Config(description = "If enabled, when eating food, the saturation will not sum, instead will just be set to the food's saturation (if higher than the current). If AppleSkin is installed it also adds compatibility for saturation restored overlay")
	public static Boolean combatSnapshotEatingSaturation = true;
	@Config(description = "If enabled, eating cakes will give 30 seconds of Speed and Haste")
	public static Boolean buffCake = true;
    @Config(description = "Adds a loot table when shearing pumpkins (insanesurvivaloverhaul:gameplay/pumpkin_shear). This also replaces seeds drop with Pumpkin Pulp")
    public static Boolean addPumpkinShearLootTable = true;

	@Config(description = "Food can no longer be smelted in furnaces and change smokers recipe to require soul sand.\nThis also enables a change to the smelt_item_function in loot tables to use smoker recipes instead of furnaces (otherwise, mobs wouldn't drop cooked food). Might have unintended side effects.")
	public static Boolean noFurnaceFoodAndSmokerRecipe = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("no_food_in_furnace", "Insane's Survival Overhaul No Food in Furnace", () -> this.isEnabled() && !Packs.disableAllDataPacks && noFurnaceFoodAndSmokerRecipe);
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

		event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0, false, false, true));
		event.getEntity().addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 600, 0, false, false, true));
	}

	private static FoodProperties lastFoodEatenCache;
	private static int lastFoodEatenTime;

	public static int getFoodConsumingTime(ItemStack stack) {
		FoodProperties food = stack.getItem().getFoodProperties(stack, null);
		if (food == lastFoodEatenCache)
			return lastFoodEatenTime;
		int ticks = eatingSpeedFormula.isBlank()
				? -1
				: (int) MCUtils.computeFoodFormula(food, eatingSpeedFormula);
		lastFoodEatenCache = food;
		//noinspection DataFlowIssue
		lastFoodEatenTime = ticks >= 0 ? ticks : (int) (food.eatSeconds() * 20f);
		return lastFoodEatenTime;
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

	/*@SubscribeEvent
	public void onEffectApply(AddEatEffectEvent event) {
		for (CustomFoodProperties foodValue : customFoodProperties) {
			if (foodValue.effects == null || !foodValue.food.matchesItem(event.getStack().getItem()))
				continue;
			foodValue.getEffects().forEach(pair -> {
				if (!event.getLevel().isClientSide && pair.getFirst() != null && event.getLevel().random.nextFloat() < pair.getSecond()) {
					event.getEntity().addEffect(new MobEffectInstance(pair.getFirst().getMobEffectInstance()));
				}
			});
			event.setCanceled(true);
			break;
		}
	}*/

	private static void processFoodMultipliers(Map<Item, DataComponentPatch> patches) {
		boolean applyFormulas = !foodHungerFormula.isBlank() || !foodSaturationFormula.isBlank();
		boolean applyRawPoison = rawFoodPoisonChance > 0;
		if (!applyFormulas && !applyRawPoison)
			return;

		for (Item item : BuiltInRegistries.ITEM) {
			FoodProperties food = item.components().get(DataComponents.FOOD);
			if (food == null)
				continue;

			int newNutrition = !foodHungerFormula.isBlank()
					? (int) MCUtils.computeFoodFormula(food, foodHungerFormula)
					: food.nutrition();
			float newSaturation = !foodSaturationFormula.isBlank()
					? (float) MCUtils.computeFoodFormula(food, foodSaturationFormula)
					: food.saturation();

			List<FoodProperties.PossibleEffect> newEffects = food.effects();
			if (applyRawPoison && isRawFood(item)) {
				int duration = (int) ((newNutrition + newSaturation) * 20 * rawFoodPoisonDurationMultiplier);
				newEffects = new ArrayList<>(food.effects());
				newEffects.add(new FoodProperties.PossibleEffect(() -> new MobEffectInstance(MobEffects.POISON, duration, rawFoodPoisonAmplifier), rawFoodPoisonChance.floatValue()));
			}

			if (newNutrition == food.nutrition() && newSaturation == food.saturation() && newEffects == food.effects())
				continue;

			FoodProperties modified = new FoodProperties(newNutrition, newSaturation, food.canAlwaysEat(), food.eatSeconds(), food.usingConvertsTo(), newEffects);
			patches.put(item, DataComponentPatch.builder().set(DataComponents.FOOD, modified).build());
		}
	}

	/*@SuppressWarnings("ConstantConditions")
	public static void processCustomFoodValues(List<CustomFoodProperties> list, boolean isClientSide) {
		if (!originalFoodProperties.isEmpty()) {
			originalFoodProperties.forEach((foodProperty, originalFoodProperty) -> {
				foodProperty.nutrition = originalFoodProperty.nutrition;
				foodProperty.saturationModifier = originalFoodProperty.saturationModifier;
			});
		}
		else {
			ForgeRegistries.ITEMS.getValues()
					.stream().filter(item -> item.getFoodProperties() != null)
					.forEach(item -> {
						if (originalFoodProperties.containsKey(item.getFoodProperties()))
							return;
						FoodProperties properties = item.getFoodProperties();
						if (!FMLLoader.isProduction())
							ISOLogHelper.debug("Storing original food properties for item " + item.getDescriptionId() + ": hunger: " + properties.nutrition + ", saturationMod: " + properties.saturationModifier + ", saturation: " + insane96mcp.insanelib.util.MCUtils.getFoodSaturationRestored(properties));
						originalFoodProperties.put(properties, new FoodProperties.Builder().nutrition(properties.nutrition).saturationMod(properties.saturationModifier).build());
					});
		}
		if (!list.isEmpty()) {
			for (CustomFoodProperties foodValue : list)
				foodValue.apply();
		}
		//reset cache when reloading
		customFoodPropertiesCache = null;
		processFoodMultipliers(isClientSide);
	}*/

	public static boolean isRawFood(Item item) {
		return item.builtInRegistryHolder().is(RAW_FOOD);
	}
}
