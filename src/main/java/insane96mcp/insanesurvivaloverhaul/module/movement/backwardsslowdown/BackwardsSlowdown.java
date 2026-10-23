package insane96mcp.insanesurvivaloverhaul.module.movement.backwardsslowdown;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@LoadFeature(module = ISOModules.MOVEMENT, description = "Players's slowed down when walking backwards.")
public class BackwardsSlowdown extends Feature {

	private static final ResourceLocation BACKWARD_WALK_SLOWDOWN_ID = InsaneSO.id("backwards_slowdown");

	@Config(min = 0d, max = 1d, description = "How much slower will the player go when walking backwards.")
	public static Double slowdown = 0.3d;

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Pre event) {
		//Don't apply it server side from here since the server doesn't know the zza of the player
		if (!event.getEntity().level().isClientSide
				|| !event.getEntity().isLocalPlayer())
			return;

		Player player = event.getEntity();
		//Flying, riding or the feature being disabled count as not walking backwards, so the modifier gets removed
		boolean backwards = this.isEnabled()
				&& slowdown != 0d
				&& !player.getAbilities().flying
				&& !player.isPassenger()
				&& player.zza < 0f;
		//Only update (and sync to the server) when walking backwards starts or stops
		if (backwards == player.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(BACKWARD_WALK_SLOWDOWN_ID))
			return;

		applyModifier(player, backwards);
		ServerboundBackwardsSlowdownPacket.sync(backwards);
	}

	public static void applyModifier(Player player, boolean backwards) {
		player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(BACKWARD_WALK_SLOWDOWN_ID);
		if (backwards)
			MCUtils.applyModifier(player, Attributes.MOVEMENT_SPEED, BACKWARD_WALK_SLOWDOWN_ID, -slowdown, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
	}
}