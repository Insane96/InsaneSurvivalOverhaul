package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@ConfigType(ModConfig.Type.CLIENT)
@LoadFeature(module = ISOModules.COMBAT, canBeDisabled = false)
public class ShieldsClient extends Feature {
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemTooltips(ItemTooltipEvent event) {
		if (!isEnabled(Shields.class)
				|| Shields.shieldBlockDamage == 0
				|| !event.getItemStack().is(Items.SHIELD))
			return;

		event.getToolTip().add(CommonComponents.space().append(Component.translatable(InsaneSO.lang("shield_blocked_damage"), Shields.shieldBlockDamage).withStyle(ChatFormatting.DARK_GREEN)));
	}
}
