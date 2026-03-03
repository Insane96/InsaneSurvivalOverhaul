package insane96mcp.insanesurvivaloverhaul.mixin.module.world.fluids;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.insanesurvivaloverhaul.module.world.Fluids;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.WaterFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WaterFluid.class)
public abstract class WaterFluidMixin_Fluid extends FlowingFluid {

	/**
	 * Reduces water's explosion resistance to 0 when fluid explosion resistance changes are
	 * enabled, allowing explosions to propagate through water.
	 */
	@ModifyReturnValue(method = "getExplosionResistance", at = @At("RETURN"))
	private float insanesurvivaloverhaul$onWaterExplosionResistance(float original) {
		if (!Fluids.shouldChangeFluidsExplosionResistance())
			return original;
		return 0f;
	}
}
