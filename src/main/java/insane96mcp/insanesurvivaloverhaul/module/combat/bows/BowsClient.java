package insane96mcp.insanesurvivaloverhaul.module.combat.bows;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.event.PlayerUseItemMovSpeedEvent;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;

@ConfigType(ModConfig.Type.CLIENT)
@LoadFeature(module = ISOModules.COMBAT, canBeDisabled = false)
public class BowsClient extends Feature {
	@SubscribeEvent
	public void onPlayerUseItemSpeedModifier(PlayerUseItemMovSpeedEvent event) {
		if (!isEnabled(Bows.class)
				|| !event.getUseItem().is(Bows.SHORTBOW.get()))
			return;

		event.setSpeedModifier(0.75f);
	}
}
