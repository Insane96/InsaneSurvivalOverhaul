package insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

@LoadFeature(module = ISOModules.MINING, description = "Adds recipes for crafting in an anvil through data packs")
public class AnvilCrafting extends Feature {
	@Config(description = "Enables a data pack that replaces vanilla metal equipment recipes requiring an anvil")
	public static Boolean metalEquipmentInAnvil = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("equipment_forging", "Insane's Survival Overhaul Equipment Forging", () -> this.isEnabled() && metalEquipmentInAnvil && !Packs.disableAllDataPacks);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onAnvilUpdate(AnvilUpdateEvent event) {
		for (AnvilRecipe anvilRecipe : AnvilRecipeReloadListener.RECIPES) {
			if (anvilRecipe.leftIngredient.test(event.getLeft()) && anvilRecipe.rightIngredient.test(event.getRight())) {
				event.setCost(0);
				ItemStack result = anvilRecipe.result.copy();
				if (anvilRecipe.keepDurability)
					result.setDamageValue(event.getLeft().getDamageValue());
				event.setOutput(result);
			}
		}
	}
}
