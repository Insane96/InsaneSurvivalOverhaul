package insane96mcp.insanesurvivaloverhaul.module.movement;

/**
 * Implemented on {@link net.minecraft.world.entity.Entity} via mixin, stores the ledge being climbed by {@link LedgeClimbing}.
 */
public interface ILedgeClimber {
	/**
	 * @return the absolute Y of the top of the ledge being climbed, or {@link Double#NaN} if not climbing
	 */
	double insanesurvivaloverhaul$getLedgeTopY();

	void insanesurvivaloverhaul$setLedgeTopY(double y);
}
