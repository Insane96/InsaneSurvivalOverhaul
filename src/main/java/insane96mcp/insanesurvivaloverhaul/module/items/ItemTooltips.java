package insane96mcp.insanesurvivaloverhaul.module.items;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@LoadFeature(module = ISOModules.ITEMS, description = "Add item tooltips via item tags. Items in the insanesurvivaloverhaul:has_tooltip item tag will get a tooltip with the vanilla name + .tooltip (e.g. item.minecraft.arrow.tooltip)")
public class ItemTooltips extends Feature {
	public static final TagKey<Item> HAS_TOOLTIP = ISOItemTagsProvider.create("has_tooltip");

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemTooltips(ItemTooltipEvent event) {
		if (!this.isEnabled()
				|| !event.getItemStack().is(HAS_TOOLTIP))
			return;

		event.getToolTip().add(1, Component.translatable(event.getItemStack().getItem().getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
	}
}