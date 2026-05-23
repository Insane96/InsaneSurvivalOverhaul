package insane96mcp.insanesurvivaloverhaul.module.movement;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.resources.ResourceLocation;

@LoadFeature(module = ISOModules.MOVEMENT)
public final class Sprinting extends Feature {
	public static final ResourceLocation SPRINT_PENALTY_ID = InsaneSO.id("hungry_sprint_penalty");

	@Config(min = 0, max = 20, description = "Player can only sprint when have at least this much hunger. Vanilla is 7")
	public static Integer minHunger = 1;
	@Config(min = 0, max = 20, description = "Movement speed penalty when below this hunger")
	public static Integer speedPenaltyBelowHunger = 7;
	@Config(min = 0, description = "How much less movement speed per hunger below 'Speed Penalty below hunger' sprinting players have. Vanilla gives 0.3 bonus movement speed when sprinting")
	public static Double speedReductionEachHunger = 0.025;
}