package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VehicleEntity.class)
public class VehicleEntityMixin_Tweaks {
    /**
     * Replaces the hardcoded damage threshold (40.0) required to break a vehicle with the configurable value from {@link Tweaks#damageToBreakVehicles}.
     */
    @ModifyExpressionValue(method = "hurt", at = @At(value = "CONSTANT", args = "floatValue=40.0"))
    public float insanesurvivaloverhaul$damageToBreak(float damageToBreak) {
        return Tweaks.damageToBreakVehicles * 10f;
    }
}
