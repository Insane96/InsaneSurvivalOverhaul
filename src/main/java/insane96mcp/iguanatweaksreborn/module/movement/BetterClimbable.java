package insane96mcp.iguanatweaksreborn.module.movement;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

@LoadFeature(module = Modules.Ids.MOVEMENT, description = "Players's slides down climbable blocks faster and stands still when opening an interface")
public class BetterClimbable extends Feature {

	@Config(min = 0, max = 5d, description = "Speed at which players move up climbable blocks. Vanilla is 0.2")
	public static Double climbSpeed = 0.3d;
	@Config(min = 0, max = 5d, description = "How much faster players move down climbable blocks. This is disabled if quark is present")
	public static Double descendSpeed = 0.2d;
	@Config(min = 0, max = 5d, description = "If true, and not looking down, player will crouch when opening an interface. This is disabled if quark is present")
	public static Boolean crouchWhenOpeningInterface = true;

	@Config(description = "Entities will not count as on climbable when on the ground, preventing slowdown when passing through climbable blocks.")
	public static Boolean notOnClimbableWhenOnGround = true;

	@Config(description = "If enabled you'll only be able to climb when pressing jump and not when against a wall and moving.")
	public static Boolean onlyClimbWithJump = true;

	@Config(description = "If enabled, climbables will no longer cancel fall damage. Damage is reduced by 40%.")
	public static Boolean fallDamageOnClimbables = true;

	public BetterClimbable(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (!this.isEnabled()
				|| ModList.get().isLoaded("quark")
				|| event.phase == TickEvent.Phase.END
				|| !(event.player instanceof LocalPlayer localPlayer))
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
				|| ModList.get().isLoaded("quark")
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