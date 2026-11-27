package insane96mcp.insanesurvivaloverhaul.module.items;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting.RuneEnchantingIntegration;
import insane96mcp.insanesurvivaloverhaul.setup.ModIds;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

@LoadFeature(module = ISOModules.ITEMS, description = "Prevents enchanted items from being wasted as crafting ingredients, as fuel or by smelting. If Rune Enchanting is installed, items with runes count as enchanted.")
public class EnchantedItemsRestrictions extends Feature {

	@Config(description = "If true, enchanted items can't be used in crafting recipes (e.g. an enchanted bow to craft a dispenser). Special recipes, such as repairing, are not affected.")
	public static Boolean noCrafting = true;
	@Config(description = "If true, enchanted items can't be used as furnace fuel.")
	public static Boolean noFuel = true;
	@Config(description = "If true, enchanted items can't be smelted (e.g. an enchanted golden sword into nuggets).")
	public static Boolean noSmelting = true;

	private static boolean runeEnchantingLoaded;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		runeEnchantingLoaded = ModList.get().isLoaded(ModIds.RUNE_ENCHANTING);
	}

	public static boolean isEnchantedOrRuned(ItemStack stack) {
		return stack.isEnchanted() || (runeEnchantingLoaded && RuneEnchantingIntegration.hasRunes(stack));
	}

	public static boolean hasRunes(ItemStack stack) {
		return runeEnchantingLoaded && RuneEnchantingIntegration.hasRunes(stack);
	}

	public static boolean preventsCrafting(CraftingInput input) {
		if (!noCrafting || !isEnabled(EnchantedItemsRestrictions.class))
			return false;
		for (ItemStack stack : input.items()) {
			if (!stack.isEmpty() && isEnchantedOrRuned(stack))
				return true;
		}
		return false;
	}

	public static boolean preventsSmelting(ItemStack stack) {
		return noSmelting && isEnabled(EnchantedItemsRestrictions.class) && isEnchantedOrRuned(stack);
	}

	@SubscribeEvent
	public void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
		if (!this.isEnabled()
				|| !noFuel
				|| event.getBurnTime() <= 0
				|| !isEnchantedOrRuned(event.getItemStack()))
			return;

		event.setBurnTime(0);
	}
}
