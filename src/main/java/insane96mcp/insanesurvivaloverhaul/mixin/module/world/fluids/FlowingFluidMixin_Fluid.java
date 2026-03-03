package insane96mcp.insanesurvivaloverhaul.mixin.module.world.fluids;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.world.Fluids;
import net.minecraft.world.level.material.FlowingFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FlowingFluid.class)
public class FlowingFluidMixin_Fluid {
	/**
	 * Forces the solid-face check in {@code getFlow} to return {@code true} when water push
	 * is configured to work regardless of surrounding blocks, enabling water currents to push
	 * entities even when no solid blocks are adjacent to the fluid.
	 */
	@ModifyExpressionValue(method = "getFlow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FlowingFluid;isSolidFace(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
	public boolean insanesurvivaloverhaul$forceWaterPushFlow(boolean original) {
		return Fluids.shouldWaterPushWhenNoBlocksAround() || original;
	}
}
