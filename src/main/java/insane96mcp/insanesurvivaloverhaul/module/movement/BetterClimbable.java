package insane96mcp.insanesurvivaloverhaul.module.movement;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;

@LoadFeature(module = ISOModules.MOVEMENT, description = "Players's slides down climbable blocks faster and stands still when opening an interface")
public class BetterClimbable extends Feature {

	@Config(min = 0, max = 5d, description = "Speed at which players move up climbable blocks. Vanilla is 0.2")
	public static Double climbSpeed = 0.3d;
	@Config(min = 0, max = 5d, description = "How much faster players move down climbable blocks.")
	public static Double descendSpeed = 0.2d;
	@Config(min = 0, max = 5d, description = "If true, and not looking down, player will crouch when opening an interface.")
	public static Boolean crouchWhenOpeningInterface = true;

	@Config(description = "Entities will not count as on climbable when on the ground, preventing slowdown when passing through climbable blocks.")
	public static Boolean notOnClimbableWhenOnGround = true;

	@Config(description = "If enabled you'll only be able to climb when pressing jump and not when against a wall and moving.")
	public static Boolean onlyClimbWithJump = true;
}