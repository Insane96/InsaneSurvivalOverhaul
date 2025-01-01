package insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks;

import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.data.ITRMobEffectInstance;
import insane96mcp.iguanatweaksreborn.data.generator.ITRItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.combat.RegeneratingAbsorption;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.ITRRegistries;
import insane96mcp.iguanatweaksreborn.setup.IntegratedPack;
import insane96mcp.iguanatweaksreborn.utils.Utils;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.IdTagMatcher;
import insane96mcp.insanelib.event.AddEatEffectEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Foods & Drinks", description = "Changes to food nourishment and the speed on how food is eaten or how items are consumed. Custom Food Properties are controlled via json in this feature's folder. Removing entries from the json requires a minecraft restart.")
@LoadFeature(module = Modules.Ids.HUNGER_HEALTH)
public class FoodDrinks extends JsonFeature {

	public static final RegistryObject<Item> BROWN_MUSHROOM_STEW = ITRRegistries.ITEMS.register("brown_mushroom_stew", () -> new BowlFoodItem(new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(3).saturationMod(0.6F).build())
	));
	public static final RegistryObject<Item> RED_MUSHROOM_STEW = ITRRegistries.ITEMS.register("red_mushroom_stew", () -> new BowlFoodItem(new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(3).saturationMod(0.6F).build())
	));
	public static final RegistryObject<Item> NETHERIZED_STEW = ITRRegistries.ITEMS.register("netherized_stew", () -> new BowlFoodItem(new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(12).saturationMod(1.4F).effect(() -> new MobEffectInstance(MobEffects.POISON, 30 * 20, 0), 0.8f).build())
	));

	public static final RegistryObject<Item> OVER_EASY_EGG = ITRRegistries.ITEMS.register("over_easy_egg", () -> new Item(new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6F).build())
	));

	public static final RegistryObject<Item> PUMPKIN_PULP = ITRRegistries.ITEMS.register("pumpkin_pulp", () -> new Item(new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3F).build())
	));

	public static final TagKey<Item> RAW_FOOD = ITRItemTagsProvider.create("raw_food");
	public static final TagKey<Item> FOOD_BLACKLIST = ITRItemTagsProvider.create("food_drinks_no_hunger_changes");

	public static final ArrayList<CustomFoodProperties> CUSTOM_FOOD_PROPERTIES_DEFAULT = new ArrayList<>(List.of(
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:rotten_flesh")).setNutrition(2).setEatingTime(55).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:spider_eye")).setNutrition(1).setEatingTime(40).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:honey_bottle")).setNutrition(2).alwaysEat(false).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:pumpkin_pie")).setNutrition(6).setEatingTime(40).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:golden_apple"))
					.addEffect(new ITRMobEffectInstance.Builder(MobEffects.REGENERATION, 100).setAmplifier(1).build())
					.addEffect(new ITRMobEffectInstance.Builder(RegeneratingAbsorption.EFFECT, 2400).build()).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("minecraft:enchanted_golden_apple"))
					.addEffect(new ITRMobEffectInstance.Builder(MobEffects.REGENERATION, 400).setAmplifier(1).build())
					.addEffect(new ITRMobEffectInstance.Builder(MobEffects.DAMAGE_RESISTANCE, 6000).build())
					.addEffect(new ITRMobEffectInstance.Builder(MobEffects.FIRE_RESISTANCE, 6000).build())
					.addEffect(new ITRMobEffectInstance.Builder(RegeneratingAbsorption.EFFECT, 2400).setAmplifier(3).build()).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("berry_good:sweet_berry_meatballs")).setNutrition(9).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("berry_good:glowgurt")).setNutrition(8).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("farmersdelight:bone_broth")).setNutrition(6).build(),
			new CustomFoodProperties.Builder(IdTagMatcher.newId("autumnity:pumpkin_bread")).setNutrition(5).build()
	));
	public static final ArrayList<CustomFoodProperties> customFoodProperties = new ArrayList<>();

	//TODO Change this to formula
	@Config(min = 0d, max = 20d)
	@Label(name = "Food Hunger Multiplier", description = "Food's hunger restored will be multiplied by this value (rounded up). E.g. With this set to 0.5 a Cooked Pork-chop would restore 4 hunger instead of 8. Setting to 1 will disable this feature.")
	public static Double foodHungerMultiplier = 1d;
	//TODO Change this to formula
	@Config(min = 0d, max = 64d)
	@Label(name = "Food Saturation Multiplier", description = "Food's saturation restored will be multiplied by this value. Be aware that saturation is a multiplier and not a flat value, it is used to calculate the effective saturation restored when a player eats, and this calculation includes hunger, so by reducing hunger you automatically reduce saturation too. Setting to 1 will disable this feature.\nThis requires a Minecraft Restart.")
	public static Double foodSaturationMultiplier = 1d;

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
	@Config(min = 0d, max = 1f)
	@Label(name = "Raw food Poison Chance", description = "Raw food has this chance to poison the player. Raw food is defined in the iguanatweaksreborn:raw_food tag")
	public static Double rawFoodPoisonChance = 0.7d;

	@Config
	@Label(name = "No Furnace food and smoker recipe", description = "Food can no longer be smelted in furnaces and change smokers recipe to require soul sand.\nThis also enables a change to the smelt_item_function in loot tables to use smoker recipes instead of furnaces (otherwise, mobs wouldn't drop cooked food). Might have unintended side effects.")
	public static Boolean noFurnaceFoodAndSmokerRecipe = true;

	public FoodDrinks(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "no_food_in_furnace", Component.literal("IguanaTweaks Reborn No Food in Furnace"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && noFurnaceFoodAndSmokerRecipe));
		addSyncType(new ResourceLocation(IguanaTweaksReborn.MOD_ID, "food_properties"), new SyncType(json -> loadAndReadJson(json, customFoodProperties, CUSTOM_FOOD_PROPERTIES_DEFAULT, CustomFoodProperties.LIST_TYPE)));
		JSON_CONFIGS.add(new JsonConfig<>("food_properties.json", customFoodProperties, CUSTOM_FOOD_PROPERTIES_DEFAULT, CustomFoodProperties.LIST_TYPE, FoodDrinks::processCustomFoodValues, true, new ResourceLocation(IguanaTweaksReborn.MOD_ID, "food_properties")));
	}

	@Override
	public String getModConfigFolder() {
		return IguanaTweaksReborn.CONFIG_FOLDER;
	}

	@Override
	public void loadJsonConfigs() {
		if (!this.isEnabled())
			return;
		super.loadJsonConfigs();
		processFoodMultipliers(false);
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
			player.addEffect(new MobEffectInstance(MobEffects.POISON, item.getFoodProperties(event.getItem(), player).getNutrition() * 20 * 3));
		}
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
		int ticks = (int) Utils.computeFoodFormula(food, eatingSpeedFormula);
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

	private static boolean processedFoodMultipliers = false;

	@SuppressWarnings("ConstantConditions")
	public static void processFoodMultipliers(boolean isClientSide) {
		if (processedFoodMultipliers)
			return;
		processedFoodMultipliers = true;
		for (Item item : ForgeRegistries.ITEMS.getValues()) {
			if (item.getFoodProperties() == null
					|| isItemInTag(item, FOOD_BLACKLIST, isClientSide))
				continue;
			FoodProperties food = item.getFoodProperties();
			if (foodHungerMultiplier != 1d)
				food.nutrition = (int) Math.ceil(item.getFoodProperties().getNutrition() * foodHungerMultiplier);
			food.saturationModifier = (float) (item.getFoodProperties().getSaturationModifier() * foodSaturationMultiplier);
		}

	}

	@SuppressWarnings("ConstantConditions")
	public static void processCustomFoodValues(List<CustomFoodProperties> list, boolean isClientSide) {
		if (list.isEmpty())
			return;
		for (CustomFoodProperties foodValue : list) {
			foodValue.apply();
		}
		//reset cache when reloading
		customFoodPropertiesCache = null;
	}

	public static boolean isRawFood(Item item) {
		return item.builtInRegistryHolder().is(RAW_FOOD);
	}
}
