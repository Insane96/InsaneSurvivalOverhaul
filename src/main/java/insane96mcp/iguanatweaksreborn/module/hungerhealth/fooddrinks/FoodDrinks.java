package insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.data.ISOMobEffectInstance;
import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.combat.RegeneratingAbsorption;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.iguanatweaksreborn.utils.ISOLogHelper;
import insane96mcp.iguanatweaksreborn.utils.MCUtils;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.IdTagMatcher;
import insane96mcp.insanelib.event.AddEatEffectEvent;
import insane96mcp.insanelib.event.CakeEatEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
@Label(name = "Foods & Drinks", description = "Changes to food nourishment and the speed on how food is eaten or how items are consumed. Custom Food Properties are controlled via json in this feature's folder.")
@LoadFeature(module = Modules.Ids.HUNGER_HEALTH)
public class FoodDrinks extends JsonFeature {

	public static final RegistryObject<Item> BROWN_MUSHROOM_STEW = ISORegistries.ITEMS.register("brown_mushroom_stew", () -> new BowlFoodItem(new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(3).saturationMod(0.6F).build())
	));
	public static final RegistryObject<Item> RED_MUSHROOM_STEW = ISORegistries.ITEMS.register("red_mushroom_stew", () -> new BowlFoodItem(new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(3).saturationMod(0.6F).build())
	));
	public static final RegistryObject<Item> NETHERIZED_STEW = ISORegistries.ITEMS.register("netherized_stew", () -> new BowlFoodItem(new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(12).saturationMod(1.4F).effect(() -> new MobEffectInstance(MobEffects.POISON, 30 * 20, 0), 0.8f).build())
	));

	public static final RegistryObject<Item> OVER_EASY_EGG = ISORegistries.ITEMS.register("over_easy_egg", () -> new Item(new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6F).build())
	));

	public static final RegistryObject<Item> PUMPKIN_PULP = ISORegistries.ITEMS.register("pumpkin_pulp", () -> new Item(new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3F).build())
	));

	public static final TagKey<Item> RAW_FOOD = ISOItemTagsProvider.create("raw_food");
	public static final TagKey<Item> FOOD_BLACKLIST = ISOItemTagsProvider.create("food_drinks_no_hunger_changes");

	public static final ArrayList<CustomFoodProperties> CUSTOM_FOOD_PROPERTIES_DEFAULT = new ArrayList<>(List.of(
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:rotten_flesh")).nutrition(2).setEatingTime(55).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:spider_eye")).nutrition(1).setEatingTime(40).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:honey_bottle")).nutrition(2).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:cooked_beef")).nutrition(6).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:cooked_porkchop")).nutrition(7).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:pumpkin_pie")).nutrition(6).setEatingTime(40).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:rabbit_stew")).nutrition(12).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:glow_berries")).alwaysEat(true).build(),
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
			new CustomFoodProperties.Builder(IdTagMatcher.newId("buzzier_bees:honey_bread")).nutrition(5).build()
	));
	public static final ArrayList<CustomFoodProperties> customFoodProperties = new ArrayList<>();

	@Config
	@Label(name = "Food Hunger Formula", description = "Food's hunger restored will be calculated from this formula. Variables as hunger, saturation_modifier, effectiveness as numbers and fast_food as boolean can be used. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html. Setting this to an empty string disables the feature. Can be re-applied with /reload")
	public static String foodHungerFormula = "";
	@Config
	@Label(name = "Food Saturation Modifier Formula", description = "Food's saturation multiplier will be calculated from this formula. This is not a flat value: https://minecraft.wiki/w/Hunger#Food_level_and_saturation_level_restoration. Variables as hunger, saturation_modifier, effectiveness as numbers and fast_food as boolean can be used. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html. Setting this to an empty string disables the feature. Can be re-applied with /reload")
	public static String foodSaturationModifierFormula = "saturation_modifier * 1.2";

	@Config
	@Label(name = "Faster Drink Consuming", description = "Makes potion, milk and honey faster to drink, 1 second instead of 1.6.")
	public static Boolean fasterDrinkConsuming = true;
	@Config
	@Label(name = "Eating Speed Based Off Food Restored", description = "Makes the speed for eating food based off the hunger and saturation they provide.")
	public static Boolean eatingSpeedBasedOffFood = true;
	@Config
	@Label(name = "Eating Speed Formula", description = "The formula to calculate the ticks required to eat a food. Variables as hunger, saturation_modifier, effectiveness as numbers and fast_food as boolean can be used. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html. The default formula increases the time to eat exponentially when higher effectiveness.")
	public static String eatingSpeedFormula = "MIN(MAX((IF(fast_food, 16, 32) * effectiveness) * 0.075, IF(fast_food, 12, 20)), 75)";
	@Config
	@Label(name = "Stop consuming on hit", description = "If true, eating/drinking stops when the player's hit.")
	public static Boolean stopConsumingOnHit = true;
	@Config
	@Label(description = "If true, eating will always be possible, even with full hunger.")
	public static Boolean alwaysEat = false;
	@Config(min = 0d, max = 1f)
	@Label(name = "Raw food Poison Chance", description = "Raw food has this chance to poison the player. Raw food is defined in the iguanatweaksreborn:raw_food tag")
	public static Double rawFoodPoisonChance = 0.7d;
	@Config(min = 0d, max = 255)
	@Label(name = "Raw food Poison Amplifier", description = "Raw food will give this level of poison to the player.")
	public static Integer rawFoodPoisonAmplifier = 1;
	@Config(min = 0d)
	@Label(name = "Raw food Poison Duration Multiplier", description = "Raw food's poison duration will be multiplied by this value. With this set to 1, raw food will give 1 second of poison per nutrition + saturation given.")
	public static Double rawFoodPoisonDurationMultiplier = 2d;
	@Config
	@Label(name = "Combat Snapshot eating saturation", description = "If enabled, when eating food the saturation will not sum, instead will just be set to the food's saturation (if higher than the current). If AppleSkin is installed it also adds compatibility for saturation restored overlay")
	public static Boolean combatSnapshotEatingSaturation = true;
	@Config
	@Label(name = "Buff cakes", description = "If enabled, eating cakes will give 30 seconds of Speed and Haste")
	public static Boolean buffCake = true;

	@Config
	@Label(name = "No Furnace food and smoker recipe", description = "Food can no longer be smelted in furnaces and change smokers recipe to require soul sand.\nThis also enables a change to the smelt_item_function in loot tables to use smoker recipes instead of furnaces (otherwise, mobs wouldn't drop cooked food). Might have unintended side effects.")
	public static Boolean noFurnaceFoodAndSmokerRecipe = true;

	public FoodDrinks(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		InsaneSurvivalOverhaul.addServerPack("no_food_in_furnace", "Insane's Survival Overhaul No Food in Furnace", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && noFurnaceFoodAndSmokerRecipe);
		addSyncType(new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "food_properties"), new SyncType(json -> loadAndReadJson(json, customFoodProperties, CUSTOM_FOOD_PROPERTIES_DEFAULT, CustomFoodProperties.LIST_TYPE)));
		JSON_CONFIGS.add(new JsonConfig<>("food_properties.json", customFoodProperties, CUSTOM_FOOD_PROPERTIES_DEFAULT, CustomFoodProperties.LIST_TYPE, FoodDrinks::processCustomFoodValues, true, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "food_properties")));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSurvivalOverhaul.CONFIG_FOLDER;
	}

	@Override
	public void loadJsonConfigs() {
		if (!this.isEnabled())
			return;
		super.loadJsonConfigs();
		//processFoodMultipliers(false);
	}

	@SubscribeEvent
	public void onPlayerEat(LivingEntityUseItemEvent.Finish event) {
		if (!this.isEnabled()
				|| event.getItem().getItem().getFoodProperties() == null
				|| !(event.getEntity() instanceof Player player)
				|| event.getEntity().level().isClientSide)
			return;
		Item item = event.getItem().getItem();
		if (player.getRandom().nextDouble() < rawFoodPoisonChance && isRawFood(item)) {
			//noinspection DataFlowIssue
			player.addEffect(new MobEffectInstance(MobEffects.POISON, (int) (insane96mcp.insanelib.util.MCUtils.getFoodEffectiveness(item.getFoodProperties(event.getItem(), player)) * 20 * rawFoodPoisonDurationMultiplier), rawFoodPoisonAmplifier));
		}
	}

	@SubscribeEvent
	public void onCakeEat(CakeEatEvent event) {
		if (!this.isEnabled()
				|| !buffCake)
			return;

		event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0, false, false, true));
		event.getEntity().addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 600, 0, false, false, true));
	}

	private static CustomFoodProperties customFoodPropertiesCache;
	private static FoodProperties lastFoodEatenCache;
	private static int lastFoodEatenTime;

	public static int getFoodConsumingTime(ItemStack stack) {
		//If in cache, get it
		if (customFoodPropertiesCache != null && customFoodPropertiesCache.food.matchesItem(stack.getItem())) {
			return customFoodPropertiesCache.eatingTime;
		}
		else {
			for (CustomFoodProperties cfp : customFoodProperties) {
				if (cfp.food.matchesItem(stack.getItem()) && cfp.eatingTime >= 0) {
					customFoodPropertiesCache = cfp;
					return cfp.eatingTime;
				}
			}
		}
		FoodProperties food = stack.getItem().getFoodProperties(stack, null);
		if (food == lastFoodEatenCache)
			return lastFoodEatenTime;
		int ticks = (int) MCUtils.computeFoodFormula(food, eatingSpeedFormula);
		lastFoodEatenCache = food;
		//noinspection DataFlowIssue
		lastFoodEatenTime = ticks >= 0 ? ticks : (food.isFastFood() ? 16 : 32);
		return lastFoodEatenTime;
	}

	@SubscribeEvent
	public void onPlayerHit(LivingDamageEvent event) {
		if (!this.isEnabled()
				|| !stopConsumingOnHit
				|| !(event.getSource().getEntity() instanceof LivingEntity)
				|| !(event.getEntity() instanceof Player player)
				|| (!player.getUseItem().getUseAnimation().equals(UseAnim.EAT) && !player.getUseItem().getUseAnimation().equals(UseAnim.DRINK)))
			return;
		player.stopUsingItem();
	}

	@SubscribeEvent
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
	}

	public static final Map<Item, FoodProperties> originalFoodProperties = new HashMap<>();

	@SuppressWarnings("ConstantConditions")
	public static void processFoodMultipliers(boolean isClientSide) {
		for (Item item : ForgeRegistries.ITEMS.getValues()) {
			if (item.getFoodProperties() == null
					|| isItemInTag(item, FOOD_BLACKLIST, isClientSide))
				continue;
			FoodProperties food = item.getFoodProperties();
			if (!foodHungerFormula.isEmpty())
				food.nutrition = (int) MCUtils.computeFoodFormula(food, foodHungerFormula);
			if (!foodSaturationModifierFormula.isEmpty())
				food.saturationModifier = MCUtils.computeFoodFormula(food, foodSaturationModifierFormula);
			if (!FMLLoader.isProduction())
				ISOLogHelper.debug("Food multiplier applied to item " + item.getDescriptionId() + ": hunger: " + food.nutrition + ", saturationMod: " + food.saturationModifier + ", saturation: " + insane96mcp.insanelib.util.MCUtils.getFoodSaturationRestored(food));
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static void processCustomFoodValues(List<CustomFoodProperties> list, boolean isClientSide) {
		if (!originalFoodProperties.isEmpty()) {
			originalFoodProperties.forEach((item, food) -> {
				item.getFoodProperties().nutrition = food.nutrition;
				item.getFoodProperties().saturationModifier = food.saturationModifier;
			});
		}
		if (originalFoodProperties.isEmpty()) {
			originalFoodProperties.putAll(ForgeRegistries.ITEMS.getValues()
					.stream().filter(item -> item.getFoodProperties() != null && !isItemInTag(item, FOOD_BLACKLIST, isClientSide))
					.collect(Collectors.toMap(Function.identity(), item -> {
						FoodProperties properties = item.getFoodProperties();
						if (!FMLLoader.isProduction())
							ISOLogHelper.debug("Storing original food properties for item " + item.getDescriptionId() + ": hunger: " + properties.nutrition + ", saturationMod: " + properties.saturationModifier + ", saturation: " + insane96mcp.insanelib.util.MCUtils.getFoodSaturationRestored(properties));
                        return new FoodProperties.Builder().nutrition(properties.nutrition).saturationMod(properties.saturationModifier).build();
					})));
		}
		if (!list.isEmpty()) {
			for (CustomFoodProperties foodValue : list)
				foodValue.apply();
		}
		//reset cache when reloading
		customFoodPropertiesCache = null;
		processFoodMultipliers(isClientSide);
	}

	public static boolean isRawFood(Item item) {
		return item.builtInRegistryHolder().is(RAW_FOOD);
	}
}
