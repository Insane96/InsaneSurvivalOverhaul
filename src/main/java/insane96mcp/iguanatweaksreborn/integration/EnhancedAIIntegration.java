package insane96mcp.iguanatweaksreborn.integration;

import insane96mcp.enhancedai.modules.mobs.targeting.Targeting;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.MinMax;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class EnhancedAIIntegration {
	public static void setupStatsBuffs(ModConfigEvent event) {
		Module.getFeature(Targeting.class).setConfigOption("Follow Range Override", new MinMax(0d));
		Module.getFeature(Targeting.class).setConfigOption("XRay Range Override", new MinMax(0d));
		//Read the config values
		Module.getFeature(Targeting.class).readConfig(event);
	}
}
