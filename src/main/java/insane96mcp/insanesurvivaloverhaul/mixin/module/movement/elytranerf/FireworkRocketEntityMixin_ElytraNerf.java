package insane96mcp.insanesurvivaloverhaul.mixin.module.movement.elytranerf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.movement.Elytra;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin_ElytraNerf {

	/**
	 * Replaces the 1.5 target speed multiplier in the firework elytra boost calculation.
	 * Lower values mean the firework pushes you to a lower maximum speed.
	 */
	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "doubleValue=1.5"))
	private double insanesurvivaloverhaul$fireworkBoostTargetSpeed(double original) {
		if (!Feature.isEnabled(Elytra.class))
			return original;
		return Elytra.fireworkBoostTargetSpeed;
	}

	/**
	 * Replaces the 0.5 lerp factor in the firework elytra boost calculation.
	 * Lower values mean each tick pushes you less aggressively toward the target speed.
	 */
	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "doubleValue=0.5"))
	private double insanesurvivaloverhaul$fireworkBoostLerp(double original) {
		if (!Feature.isEnabled(Elytra.class))
			return original;
		return Elytra.fireworkBoostLerp;
	}
}
