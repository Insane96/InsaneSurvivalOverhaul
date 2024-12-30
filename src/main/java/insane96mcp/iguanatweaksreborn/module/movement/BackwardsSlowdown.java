package insane96mcp.iguanatweaksreborn.module.movement;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.network.message.BackwardsSlowdownUpdate;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@Label(name = "Backwards slowdown", description = "PlayerStats's slowed down when walking backwards.")
@LoadFeature(module = Modules.Ids.MOVEMENT)
public class BackwardsSlowdown extends Feature {

	private static final UUID BACKWARD_WALK_SLOWDOWN_UUID = UUID.fromString("3c085336-5bee-465f-b4a8-6677f245e7fc");

	@Config(min = 0d, max = 1d)
	@Label(name = "Slowdown", description = "How much slower will the player go when walking backwards.")
	public static Double slowdown = 0.3d;

	public BackwardsSlowdown(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (!this.isEnabled()
				//Don't apply it server side from here since the server doesn't know the zza of the player
				|| !event.player.level().isClientSide
				|| event.player instanceof RemotePlayer
				|| event.phase != TickEvent.Phase.START
				|| slowdown == 0d
				|| event.player.getAbilities().flying
				|| event.player.isPassenger())
			return;

		applyModifier(event.player, event.player.zza);
		BackwardsSlowdownUpdate.sync((LocalPlayer) event.player);
	}

	public static void applyModifier(Player player, float zza) {
		player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(BACKWARD_WALK_SLOWDOWN_UUID);
		if (zza < 0f)
			MCUtils.applyModifier(player, Attributes.MOVEMENT_SPEED, BACKWARD_WALK_SLOWDOWN_UUID, "IguanaTweaks Reborn backward slowdown", -slowdown, AttributeModifier.Operation.MULTIPLY_BASE, false);
	}
}