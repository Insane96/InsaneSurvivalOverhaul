package insane96mcp.insanesurvivaloverhaul.module.movement;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@ConfigType(ModConfig.Type.CLIENT)
@LoadFeature(module = ISOModules.MOVEMENT, canBeDisabled = false)
public class BetterClimbableClient extends Feature {
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Pre event) {
		if (!isEnabled(BetterClimbable.class)
				|| !(event.getEntity() instanceof LocalPlayer localPlayer))
			return;

		boolean scaffold = localPlayer.level().getBlockState(localPlayer.blockPosition()).isScaffolding(localPlayer);
		if (localPlayer.isCrouching() == scaffold
				&& localPlayer.getRotationVector().x > 75f
				&& localPlayer.onClimbable()
				&& localPlayer.zza == 0f
				&& !localPlayer.input.jumping
				&& !localPlayer.onGround()
				&& !localPlayer.getAbilities().flying) {
			localPlayer.move(MoverType.SELF, new Vec3(0, -BetterClimbable.descendSpeed.floatValue(), 0));
		}
	}

	@SubscribeEvent
	public void onInput(MovementInputUpdateEvent event) {
		if(!isEnabled(BetterClimbable.class)
				|| !BetterClimbable.crouchWhenOpeningInterface)
			return;

		Player player = event.getEntity();
		if (player.onClimbable()
				&& Minecraft.getInstance().screen != null
				&& !player.getAbilities().flying
				&& !player.level().getBlockState(player.blockPosition()).isScaffolding(player)
				&& !(player.zza == 0 && player.getXRot() > 75f)
				&& !player.onGround()) {
			event.getInput().shiftKeyDown = true;
		}
	}
}
