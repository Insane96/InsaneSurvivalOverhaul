package insane96mcp.insanesurvivaloverhaul.module.movement.backwardsslowdown;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@LoadFeature(module = ISOModules.MOVEMENT, description = "Players's slowed down when walking backwards.")
public class BackwardsSlowdown extends Feature {

	private static final ResourceLocation BACKWARD_WALK_SLOWDOWN_ID = InsaneSO.location("backwards_slowdown");

	@Config(min = 0d, max = 1d, description = "How much slower will the player go when walking backwards.")
	public static Double slowdown = 0.3d;

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Pre event) {
		if (!this.isEnabled()
				//Don't apply it server side from here since the server doesn't know the zza of the player
				|| !event.getEntity().level().isClientSide
				|| event.getEntity() instanceof RemotePlayer
				|| slowdown == 0d
				|| event.getEntity().getAbilities().flying
				|| event.getEntity().isPassenger())
			return;

		applyModifier(event.getEntity(), event.getEntity().zza);
		ServerboundBackwardsSlowdownPacket.sync((LocalPlayer) event.getEntity());
	}

	public static void applyModifier(Player player, float zza) {
		player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(BACKWARD_WALK_SLOWDOWN_ID);
		if (zza < 0f)
			MCUtils.applyModifier(player, Attributes.MOVEMENT_SPEED, BACKWARD_WALK_SLOWDOWN_ID, -slowdown, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
	}
}