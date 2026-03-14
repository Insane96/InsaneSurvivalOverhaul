package insane96mcp.insanesurvivaloverhaul.module.movement;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;

@LoadFeature(module = ISOModules.MOVEMENT)
public class Swimming extends Feature {

	@Config(description = "Prevents swimming up really fast if swimming and holding the jump key. Only applies to water.")
	public static Boolean preventFastSwimUpWithJump = true;

	public static boolean shouldPreventFastSwimUpWithJump() {
		return Feature.isEnabled(Swimming.class) && preventFastSwimUpWithJump;
	}
}