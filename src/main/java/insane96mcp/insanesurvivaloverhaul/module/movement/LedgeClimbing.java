package insane96mcp.insanesurvivaloverhaul.module.movement;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@LoadFeature(module = ISOModules.MOVEMENT, description = "When players have a step height higher than vanilla, ledges higher than vanilla's step height are climbed slowly instead of being stepped up instantly.")
public class LedgeClimbing extends Feature {

	@Config(min = 0d, max = 10d, description = "Step ups higher than this are climbed instead of being instant. Vanilla step height is 0.6.")
	public static Double maxInstantStepHeight = 0.6d;
	@Config(min = 0d, max = 5d, description = "Upward speed applied while climbing a ledge, before gravity. Vanilla ladders use 0.2.")
	public static Double climbSpeed = 0.2d;

	/**
	 * If the entity should climb a ledge of the given height instead of stepping it up instantly
	 */
	public static boolean shouldClimb(Entity entity, double stepHeight) {
		return isEnabled(LedgeClimbing.class)
				&& stepHeight > maxInstantStepHeight + 1.0E-5d
				&& canClimb(entity);
	}

	/**
	 * If the entity is in a state where ledge climbing applies. Fluids, flying, gliding and riding keep vanilla behaviour.
	 */
	public static boolean canClimb(Entity entity) {
		return entity instanceof Player player
				&& !player.getAbilities().flying
				&& !player.isFallFlying()
				&& !player.isPassenger()
				&& !player.isInWater()
				&& !player.isInLava();
	}
}
