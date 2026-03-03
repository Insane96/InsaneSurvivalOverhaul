package insane96mcp.insanesurvivaloverhaul.mixin.module.world.fluids;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.insanesurvivaloverhaul.module.world.Fluids;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LavaFluid.class)
public abstract class LavaFluidMixin_Fluid extends FlowingFluid {

	/**
	 * Reduces lava's explosion resistance to 0 when fluid explosion resistance changes are
	 * enabled, allowing explosions to propagate through lava.
	 */
	@ModifyReturnValue(method = "getExplosionResistance", at = @At("RETURN"))
	private float insanesurvivaloverhaul$onLavaExplosionResistance(float original) {
		if (!Fluids.shouldChangeFluidsExplosionResistance())
			return original;
		return 0f;
	}
}
