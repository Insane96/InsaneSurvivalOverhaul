package insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.ModIds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;

import javax.annotation.Nullable;

@LoadFeature(module = ISOModules.MINING, description = "Adds recipes for crafting in an anvil through data packs")
public class AnvilCrafting extends Feature {
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

	@Nullable
	private static AnvilRecipe findRecipe(ItemStack left, ItemStack right) {
		if (!allowEnchantedItems && (left.isEnchanted() || (ModList.get().isLoaded(ModIds.RUNE_ENCHANTING) && RuneEnchantingIntegration.hasRunes(left))))
			return null;
		for (AnvilRecipe anvilRecipe : AnvilRecipeReloadListener.RECIPES) {
			if (anvilRecipe.leftIngredient.test(left) && anvilRecipe.rightIngredient.testAtLeast(right))
				return anvilRecipe;
		}
		return null;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onAnvilUpdate(AnvilUpdateEvent event) {
		if (!this.isEnabled())
			return;
		AnvilRecipe anvilRecipe = findRecipe(event.getLeft(), event.getRight());
		if (anvilRecipe == null)
			return;
		event.setCost(0);
		event.setMaterialCost(anvilRecipe.rightIngredient.count());
		ItemStack result = anvilRecipe.result.copy();
		if (anvilRecipe.keepDurability)
			result.setDamageValue(event.getLeft().getDamageValue());
		event.setOutput(result);
	}

	/**
	 * The recipe is found again from the inputs (still in the anvil when this event fires) instead of storing the experience in the output,
	 * which would make it not stack with normal items and give experience again when used in an anvil later
	 */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onAnvilRepair(AnvilRepairEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity().level() instanceof ServerLevel serverLevel))
			return;
		AnvilRecipe anvilRecipe = findRecipe(event.getLeft(), event.getRight());
		if (anvilRecipe == null
				|| !ItemStack.isSameItem(event.getOutput(), anvilRecipe.result))
			return;
		ExperienceOrb.award(serverLevel, event.getEntity().position(), MathHelper.getAmountWithDecimalChance(event.getEntity().getRandom(), anvilRecipe.experience));
	}
}
