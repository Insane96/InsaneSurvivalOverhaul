package insane96mcp.insanesurvivaloverhaul.mixin.module.world.fluids;

import insane96mcp.insanesurvivaloverhaul.module.world.Fluids;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidType.class)
public class FluidTypeMixin_Fluid {
	/**
	 * Overrides the motion scale of the water fluid type with the configured push force value
	 * from {@link Fluids#waterPushForce}, controlling how strongly water currents move entities.
	 */
	@Inject(at = @At("RETURN"), method = "motionScale", cancellable = true, remap = false)
	public void insanesurvivaloverhaul$onWaterMotionScale(Entity entity, CallbackInfoReturnable<Double> cir) {
		if ((Object) this == NeoForgeMod.WATER_TYPE.value())
			cir.setReturnValue(Fluids.waterPushForce);
	}
}
