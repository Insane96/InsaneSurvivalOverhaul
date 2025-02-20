package insane96mcp.iguanatweaksreborn.module.movement;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
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

@Label(name = "Better Climbable", description = "Players's slides down climbable blocks faster and stands still when opening an interface. This is disabled if quark is enabled")
@LoadFeature(module = Modules.Ids.MOVEMENT)
public class BetterClimbable extends Feature {

	@Config(min = 0, max = 5d)
	@Label(name = "Climb Speed", description = "Speed at which players move up climbable blocks. Vanilla is 0.2")
	public static Double climbSpeed = 0.3d;
	@Config(min = 0, max = 5d)
	@Label(name = "Descend Speed", description = "How much faster players move down climbable blocks")
	public static Double descendSpeed = 0.2d;

	@Config
	@Label(name = "Not on climbable when on ground", description = "Entities will not count as on climbable when on the ground, preventing slowdown when passing through climbable blocks.")
	public static Boolean notOnClimbableWhenOnGround = true;

	@Config
	@Label(name = "Only climb with jump", description = "If enabled you'll only be able to climb when pressing jump and not when against a wall and moving.")
	public static Boolean onlyClimbWithJump = true;

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
				|| ModList.get().isLoaded("quark"))
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