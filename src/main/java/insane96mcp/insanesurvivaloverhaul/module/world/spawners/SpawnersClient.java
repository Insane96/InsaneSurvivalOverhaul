package insane96mcp.insanesurvivaloverhaul.module.world.spawners;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@ConfigType(ModConfig.Type.CLIENT)
@LoadFeature(module = ISOModules.WORLD, canBeDisabled = false)
public class SpawnersClient extends Feature {
	@SubscribeEvent
	public void onTooltip(ItemTooltipEvent event) {
		if (!isEnabled(Spawners.class)
				|| !Spawners.reEnableWithSpawnerReactivator
				|| !event.getItemStack().is(Spawners.SPAWNER_REACTIVATOR_TAG))
			return;

		event.getToolTip().add(Spawners.SPAWNER_REACTIVATOR_MESSAGE);
	}
}
