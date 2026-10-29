package insane96mcp.insanesurvivaloverhaul.module.farming.hoes;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@ConfigType(ModConfig.Type.CLIENT)
@LoadFeature(module = ISOModules.FARMING, canBeDisabled = false)
public class HoesClient extends Feature {
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTooltip(ItemTooltipEvent event) {
		if (!isEnabled(Hoes.class))
			return;

		Integer radius = event.getItemStack().get(ISORegistries.SCYTHE_RADIUS.get());
		if (radius != null && radius > 0)
			event.getToolTip().add(CommonComponents.space().append(Component.translatable(Hoes.SCYTHE_RADIUS, radius).withStyle(ChatFormatting.DARK_GREEN)));
	}
}
