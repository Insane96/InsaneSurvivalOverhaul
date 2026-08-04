package insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.ModIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;

@LoadFeature(module = ISOModules.MINING, description = "Adds recipes for crafting in an anvil through data packs")
public class AnvilCrafting extends Feature {
	public static final ResourceLocation EXPERIENCE_YIELD = InsaneSO.id("experience_yield");

	@Config(description = "If true, enchanted items can be used to craft in the anvil. If false, only non-enchanted items can be used in an anvil. If Rune Enchanting is installed, runed items count as enchanted.")
	public static Boolean allowEnchantedItems = false;

	@Config(description = "Enables a data pack that replaces vanilla metal equipment recipes requiring an anvil")
	public static Boolean metalEquipmentInAnvil = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("equipment_forging", "Insane's Survival Overhaul Equipment Forging", () -> this.isEnabled() && metalEquipmentInAnvil && !Packs.disableAllDataPacks);
		InsaneSO.addClientPack("equipment_forging_rp", "Insane's Survival Overhaul Equipment Forging", () -> this.isEnabled() && metalEquipmentInAnvil && !Packs.disableAllDataPacks);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onAnvilUpdate(AnvilUpdateEvent event) {
		for (AnvilRecipe anvilRecipe : AnvilRecipeReloadListener.RECIPES) {
			if (anvilRecipe.leftIngredient.test(event.getLeft()) && anvilRecipe.rightIngredient.test(event.getRight())) {
				if (!allowEnchantedItems && (event.getLeft().isEnchanted() || (ModList.get().isLoaded(ModIds.RUNE_ENCHANTING) && RuneEnchantingIntegration.hasRunes(event.getLeft()))))
					return;
				event.setCost(0);
				event.setMaterialCost(anvilRecipe.rightIngredient.count());
				ItemStack result = anvilRecipe.result.copy();
				if (anvilRecipe.keepDurability)
					result.setDamageValue(event.getLeft().getDamageValue());
				ModNBTData.put(result, EXPERIENCE_YIELD, anvilRecipe.experience);
				event.setOutput(result);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onAnvilUpdate(AnvilRepairEvent event) {
		if (!ModNBTData.contains(event.getOutput(), EXPERIENCE_YIELD)
				|| !(event.getEntity().level() instanceof ServerLevel serverLevel))
			return;
		double experience = ModNBTData.get(event.getOutput(), EXPERIENCE_YIELD, Double.class);
		ExperienceOrb.award(serverLevel, event.getEntity().position(), MathHelper.getAmountWithDecimalChance(event.getEntity().getRandom(), experience));
	}
}
