package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.iguanatweaksreborn.module.world.Fluids;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LavaFluid.class)
public abstract class LavaFluidMixin extends FlowingFluid {

	@ModifyReturnValue(method = "getExplosionResistance", at = @At("RETURN"))
	private float onWaterExplosionResistance(float original) {
		if (!Fluids.shouldChangeFluidsExplosionResistance())
			return original;
		return 0f;
	}
}
