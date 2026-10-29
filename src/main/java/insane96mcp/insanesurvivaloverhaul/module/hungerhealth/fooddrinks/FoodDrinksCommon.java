package insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import net.neoforged.fml.config.ModConfig;

@ConfigType(ModConfig.Type.COMMON)
@LoadFeature(module = ISOModules.HUNGER_HEALTH, description = "Data packs of Foods & Drinks.")
public class FoodDrinksCommon extends Feature {
	@Config(description = "Enables a data pack that makes food no longer smeltable in furnaces and changes smokers recipe to require soul sand.\nThis also enables a change to the smelt_item_function in loot tables to use smoker recipes instead of furnaces (otherwise, mobs wouldn't drop cooked food). Might have unintended side effects.")
	public static Boolean noFurnaceFoodAndSmokerRecipe = true;
	@Config(description = "Enables a data pack that rebalances some foods and makes raw foods poisonous. Also allows mushroom stews to be crafted with any mushroom and removes golden apples.")
	public static Boolean foodChanges = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("no_food_in_furnace", "Insane's Survival Overhaul No Food in Furnace", FoodDrinksCommon::noFurnaceFoodAndSmokerRecipe);
		InsaneSO.addServerPack("food_changes", "Insane's Survival Overhaul Food Changes", () -> this.isEnabled() && !Packs.disableAllDataPacks && foodChanges);
	}

	/**
	 * If the no food in furnace data pack is enabled
	 */
	public static boolean noFurnaceFoodAndSmokerRecipe() {
		return isEnabled(FoodDrinksCommon.class) && !Packs.disableAllDataPacks && noFurnaceFoodAndSmokerRecipe;
	}
}
