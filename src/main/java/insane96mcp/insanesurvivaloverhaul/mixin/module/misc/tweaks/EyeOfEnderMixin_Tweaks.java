package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks;

import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EyeOfEnder.class)
public abstract class EyeOfEnderMixin_Tweaks {
	@Shadow
	private boolean surviveAfterDeath;

	/**
	 * Overrides the random 1-in-5 break chance rolled in signalTo, so the Eye of Ender always drops as an item when its life ends.
	 */
	@Inject(method = "signalTo", at = @At("TAIL"))
	private void insanesurvivaloverhaul$alwaysSurvive(BlockPos pos, CallbackInfo ci) {
		if (Tweaks.areEyesOfEnderUnbreakable())
			this.surviveAfterDeath = true;
	}
}
