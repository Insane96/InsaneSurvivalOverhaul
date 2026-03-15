package insane96mcp.insanesurvivaloverhaul.module.movement;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;

@LoadFeature(module = ISOModules.MOVEMENT)
public final class Elytra extends Feature {

	@Config(min = 0d, max = 1d, description = "How much the player is slowed down when gliding. Only applies outside the End Dimension. Vanilla is 0.99")
	public static Double airResistance = 0.975d;

	@Config(min = 0d, description = "Target speed multiplier for firework boost when gliding. Lower = slower max speed. Vanilla is 1.5")
	public static Double fireworkBoostTargetSpeed = 1.25d;

	@Config(min = 0d, description = "How aggressively each tick pushes toward target speed when firework boosting. Lower = slower acceleration. Vanilla is 0.5")
	public static Double fireworkBoostLerp = 0.2d;

	@Config(description = "If true, you can take of with Elytra and fireworks while off ground, no need to be gliding. Just jump and ")
	public static Boolean fireworkBoostOffGround = true;
}