package insane96mcp.insanesurvivaloverhaul.module.movement;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = ISOModules.MOVEMENT, description = "When players have a step height higher than vanilla, ledges higher than vanilla's step height are climbed slowly instead of being stepped up instantly.")
public class LedgeClimbing extends Feature {
	public static final ResourceLocation STEP_HEIGHT_BONUS_ID = InsaneSO.id("ledge_climbing_step_height_bonus");

	@Config(min = 0d, max = 10d, description = "Step height added to players, letting them climb ledges up to 0.6 + this blocks high. Players need to rejoin the world to apply changes.")
	public static Double playerStepHeightBonus = 0.5d;
	@Config(min = 0d, max = 10d, description = "Step ups higher than this are climbed instead of being instant. Vanilla step height is 0.6.")
	public static Double maxInstantStepHeight = 0.6d;
	@Config(min = 0d, max = 5d, description = "Upward speed applied while climbing a ledge, before gravity and air drag (0.4 climbs ~0.31 blocks per tick). Vanilla ladders use 0.2 (~0.12 blocks per tick).")
	public static Double climbSpeed = 0.4d;

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

	@SubscribeEvent
	public void onPlayerJoinLevel(EntityJoinLevelEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		//Removed first so config changes are applied
		MCUtils.removeModifier(player, Attributes.STEP_HEIGHT, STEP_HEIGHT_BONUS_ID);
		if (!this.isEnabled()
				|| playerStepHeightBonus <= 0d)
			return;
		MCUtils.applyModifier(player, Attributes.STEP_HEIGHT, STEP_HEIGHT_BONUS_ID, playerStepHeightBonus, AttributeModifier.Operation.ADD_VALUE, false);
	}
}
