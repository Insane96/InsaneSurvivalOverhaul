package insane96mcp.insanesurvivaloverhaul.module.items.unvanishableitems;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.runeenchanting.RuneHelper;
import net.minecraft.network.chat.Component;

/**
 * Rune Enchanting integration: runes on broken (unvanishable) items are inactive until the item is repaired, curses still apply.
 * Only loaded if Rune Enchanting is installed.
 */
public class UnvanishableItemsRuneEnchanting {
	public static final String BROKEN_ITEM_RUNES_LANG = InsaneSO.lang("broken_item_runes");

	public static void init() {
		RuneHelper.registerInactiveCondition(
				stack -> Feature.isEnabled(UnvanishableItems.class) && UnvanishableItems.isBroken(stack),
				Component.translatable(BROKEN_ITEM_RUNES_LANG));
	}
}
