package insane96mcp.insanesurvivaloverhaul.module.movement;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

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

	@Config(description = "If true, a data pack will be enabled that will make players take damage when falling on climbable blocks.")
	public static Boolean fallDamageOnClimbable = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("fall_damage_on_climbable", "Insane's Survival Overhaul Fall damage on climbable", () -> !Packs.disableAllDataPacks && this.isEnabled() && fallDamageOnClimbable);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Pre event) {
		if (!this.isEnabled()
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
			localPlayer.move(MoverType.SELF, new Vec3(0, -descendSpeed.floatValue(), 0));
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onInput(MovementInputUpdateEvent event) {
		if(!this.isEnabled()
				|| !crouchWhenOpeningInterface)
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