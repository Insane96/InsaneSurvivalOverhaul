package insane96mcp.insanesurvivaloverhaul.module.items;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import static insane96mcp.insanesurvivaloverhaul.module.items.UnvanishableItems.*;

@ConfigType(ModConfig.Type.CLIENT)
@LoadFeature(module = ISOModules.ITEMS, canBeDisabled = false)
public class UnvanishableItemsClient extends Feature {
	@Config(description = "Items with durability get a durability tooltip. Items in the `insanesurvivaloverhaul:no_durability_tooltip` item tag will not get the tooltip.")
	public static Boolean durabilityTooltip = true;

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onTooltip(ItemTooltipEvent event) {
		if (!isEnabled(UnvanishableItems.class)
                || event.getItemStack().is(NO_DURABILITY_TOOLTIP))
			return;

		ItemStack stack = event.getItemStack();

		if (stack.isDamageableItem()) {
			int durability = stack.getMaxDamage();
			if (isUnvanishable(stack))
				durability--;
			int durabilityLeft = durability - stack.getDamageValue();
			MutableComponent component = null;
            if (isBroken(stack)) {
				if (durabilityLeft < -stack.getDamageValue() * 0.05f)
					component = Component.translatable(HEAVILY_BROKEN_DURABILITY).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD);
				else
					component = Component.translatable(BROKEN_DURABILITY_LANG).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD);
			}
            else if (durabilityTooltip)
				component = Component.translatable(TOOL_DURABILITY_LANG, durabilityLeft, durability).withStyle(ChatFormatting.GRAY);
			if (component != null)
				event.getToolTip().add(event.getToolTip().size(), component);
		}
	}
}
