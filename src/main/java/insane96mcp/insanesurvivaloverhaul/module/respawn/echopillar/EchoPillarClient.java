package insane96mcp.insanesurvivaloverhaul.module.respawn.echopillar;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@ConfigType(ModConfig.Type.CLIENT)
@LoadFeature(module = ISOModules.RESPAWN, canBeDisabled = false)
public class EchoPillarClient extends Feature {
	@SubscribeEvent
	public void onTooltip(ItemTooltipEvent event) {
		if (!isEnabled(EchoPillar.class)
				|| !event.getItemStack().is(EchoPillar.ECHO_PILLAR_CATALYST))
			return;

		event.getToolTip().add(EchoPillar.ECHO_PILLAR_CATALYST_MESSAGE);
	}
}
