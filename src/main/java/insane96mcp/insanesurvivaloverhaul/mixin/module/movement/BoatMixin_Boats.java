package insane96mcp.insanesurvivaloverhaul.mixin.module.movement;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.movement.Boats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Boat.class)
public abstract class BoatMixin_Boats extends Entity {
	public BoatMixin_Boats(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	/**
	 * Overrides the ground friction to disable ice boat speed boosting.
	 */
	@ModifyReturnValue(at = @At("RETURN"), method = "getGroundFriction")
	public float insanesurvivaloverhaul$disableIceBoats(float original) {
		return Boats.getBoatFriction(original);
	}

	/**
	 * When {@link Boats} is disabled, prevents boats from taking fall damage on land by replacing the {@code ON_LAND} status with {@code IN_AIR}.
	 */
	@ModifyExpressionValue(method = "checkFallDamage", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/vehicle/Boat$Status;ON_LAND:Lnet/minecraft/world/entity/vehicle/Boat$Status;", opcode = Opcodes.GETSTATIC))
	public Boat.Status insanesurvivaloverhaul$onCheckStatus(Boat.Status original) {
		if (!Feature.isEnabled(Boats.class))
			return original;
		return Boat.Status.IN_AIR;
	}

	/**
	 * When {@link Boats} is disabled, gates boat breaking on fall damage behind the {@link Boats#breakHeight} threshold.
	 */
	@ModifyExpressionValue(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;isRemoved()Z"))
	public boolean insanesurvivaloverhaul$onCheckRemoved(boolean original) {
		if (!Feature.isEnabled(Boats.class))
			return original;
		return original && this.fallDistance >= Boats.breakHeight;
	}
}
