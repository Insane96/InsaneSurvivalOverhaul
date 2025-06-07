package insane96mcp.iguanatweaksreborn.module.items.misc;

import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.event.ISOEventFactory;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.ITEMS, description = "Control Items properties via data packs. Add more tooltips.")
public class MiscItem extends Feature {

	public static final TagKey<Item> HAS_TOOLTIP = ISOItemTagsProvider.create("has_tooltip");
	public static final String TOOL_MINING_SPEED_LANG = "iguanatweaksreborn.tool_mining_speed";

	@Config(description = "Tools get a mining speed tooltip.")
	public static Boolean miningSpeedTooltip = true;

	@Config(description = "If enabled items in the iguanatweaksreborn:has_tooltip item tag will get a tooltip with the vanilla name + .tooltip (e.g. item.minecraft.arrow.tooltip)")
	public static Boolean itemTooltips = true;

	public MiscItem(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onAttributeEvent(ItemAttributeModifierEvent event) {
		if (!this.isEnabled())
			return;

		for (ItemDefinition itemDefinition : ItemDefinitionsReloadListener.getDefinitions()) {
			itemDefinition.applyAttributes(event, event.getItemStack(), event.getModifiers());
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled()
				|| !miningSpeedTooltip)
			return;

		ItemStack stack = event.getItemStack();
		if (stack.getItem() instanceof DiggerItem diggerItem){
			float toolEfficiency = ISOEventFactory.getEfficiencyWithEnchantments(event.getEntity(), null, stack, diggerItem.speed);
			event.getToolTip().add(CommonComponents.space().append(Component.translatable(TOOL_MINING_SPEED_LANG, InsaneLib.ONE_DECIMAL_FORMATTER.format(toolEfficiency))).withStyle(ChatFormatting.DARK_GREEN));
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemTooltips(ItemTooltipEvent event) {
		if (!this.isEnabled()
				|| !itemTooltips
				|| !event.getItemStack().is(HAS_TOOLTIP))
			return;

		event.getToolTip().add(1, Component.translatable(event.getItemStack().getItem().getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
	}
}