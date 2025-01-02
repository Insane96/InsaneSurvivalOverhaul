package insane96mcp.iguanatweaksreborn.module.items.misc;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.IntegratedPack;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Misc", description = "Control Items properties via data packs. Add more tooltips.")
@LoadFeature(module = Modules.Ids.ITEMS)
public class MiscItem extends Feature {

	public static final String TOOL_EFFICIENCY_LANG = "iguanatweaksreborn.tool_efficiency";

	@Config
	@Label(name = "Efficiency tooltip", description = "Tools get an efficiency tooltip.")
	public static Boolean efficiencyTooltip = true;
	@Config
	@Label(name = "Item Stats Data Pack", description = "Enables a data pack that rebalances all the items, from armor to efficiency to weapons. Also changes some item stacks.")
	public static Boolean itemStatsDataPack = true;

	public MiscItem(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "item_stats", Component.literal("Insane's Survival Overhaul Item Stats"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && itemStatsDataPack));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onAttributeEvent(ItemAttributeModifierEvent event) {
		if (!this.isEnabled())
			return;

		for (ItemDefinition itemDefinition : ItemDefinitionsReloadListener.DEFINITIONS) {
			itemDefinition.applyAttributes(event, event.getItemStack(), event.getModifiers());
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled()
				|| !efficiencyTooltip)
			return;

		ItemStack stack = event.getItemStack();
		if (stack.getItem() instanceof DiggerItem diggerItem){
			int lvl = stack.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY);
			float toolEfficiency = diggerItem.speed;
			float bonusToolEfficiency = EnchantmentsFeature.getEfficiencyBonus(toolEfficiency, lvl);
			if (lvl > 0)
				toolEfficiency += bonusToolEfficiency;
			event.getToolTip().add(CommonComponents.space().append(Component.translatable(TOOL_EFFICIENCY_LANG, InsaneLib.ONE_DECIMAL_FORMATTER.format(toolEfficiency))).withStyle(ChatFormatting.DARK_GREEN));
		}
	}
}