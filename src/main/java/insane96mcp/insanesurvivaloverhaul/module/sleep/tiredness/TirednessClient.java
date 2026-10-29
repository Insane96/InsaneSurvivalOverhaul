package insane96mcp.insanesurvivaloverhaul.module.sleep.tiredness;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

import java.text.DecimalFormat;

@ConfigType(ModConfig.Type.CLIENT)
@LoadFeature(module = ISOModules.SLEEP, canBeDisabled = false)
public class TirednessClient extends Feature {
	@SubscribeEvent
	public void debugScreen(CustomizeGuiOverlayEvent.DebugText event) {
		if (!isEnabled(Tiredness.class))
			return;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer playerEntity = mc.player;
		if (playerEntity == null)
			return;
		if (!mc.showOnlyReducedInfo()) {
			event.getLeft().add(String.format("Tiredness: %s", new DecimalFormat("#.#").format(TirednessHandler.get(playerEntity))));
		}
	}
}
