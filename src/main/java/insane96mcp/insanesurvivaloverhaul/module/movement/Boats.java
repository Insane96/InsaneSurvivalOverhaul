package insane96mcp.insanesurvivaloverhaul.module.movement;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;

@LoadFeature(module = ISOModules.MOVEMENT)
public class Boats extends Feature {

	@Config(description = "If true, boats will no longer go stupidly fast on ice. (If quark is present this is disabled)")
	public static Boolean noIceBoat = true;
	@Config(min = 0d, description = "If true, boats will always break when falling from this height or more instead of being glitched")
	public static Double breakHeight = 5d;

	public static float getBoatFriction(float glide) {
		return Feature.isEnabled(Boats.class) && noIceBoat ? 0.45f : glide;
	}
}