package insane96mcp.iguanatweaksreborn.mixin.client;

import insane96mcp.iguanatweaksreborn.module.client.Death;
import insane96mcp.iguanatweaksreborn.module.client.Misc;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow public abstract void move(double pDistanceOffset, double pVerticalOffset, double pHorizontalOffset);

	@Shadow protected abstract double getMaxZoom(double pStartingDistance);

	@Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(DDD)V", ordinal = 0, shift = At.Shift.AFTER))
	public void onCameraSetup(BlockGetter pLevel, Entity pEntity, boolean pDetached, boolean pThirdPersonReverse, float pPartialTick, CallbackInfo ci) {
		if (!(pEntity instanceof Player player)
				|| !player.isDeadOrDying()
				|| !Feature.isEnabled(Misc.class)
				|| !Death.thirdPerson)
			return;

		this.move(-this.getMaxZoom(4d), -player.getBbHeight() / 2d, 0.0d);
	}
}
