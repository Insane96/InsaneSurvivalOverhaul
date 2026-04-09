package insane96mcp.insanesurvivaloverhaul.mixin.module.world.fluids;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanesurvivaloverhaul.module.world.Fluids;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Entity.class)
public class EntityMixin_Fluid {
    @Shadow
    protected Object2DoubleMap<FluidType> forgeFluidTypeHeight;

    /**
     * Reduces the water fall-damage modifier from 1.0 to 0.75 when the entity is submerged
     * in water and the override is enabled, so water absorbs some fall damage.
     */
    @ModifyConstant(method = "updateInWaterStateAndDoFluidPushing", constant = @Constant(floatValue = 1f))
    private float insanesurvivaloverhaul$onFluidFallModifier(float waterFallDamageModifier) {
        return Fluids.shouldOverrideWaterFallDamageModifier() && this.forgeFluidTypeHeight.object2DoubleEntrySet().stream().anyMatch(e -> e.getKey().equals(NeoForgeMod.WATER_TYPE.value())) ? 0.75f : waterFallDamageModifier;
    }

    /**
     * Prevents fall distance from being reset when an entity enters water during movement,
     * allowing fall damage to be calculated and applied upon water entry.
     */
    @WrapOperation(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;resetFallDistance()V"))
    private void insanesurvivaloverhaul$onResetFallDistanceInMove(Entity instance, Operation<Void> original) {
        if (Fluids.shouldOverrideWaterFallDamageModifier())
            return;
        original.call(instance);
    }

    /**
     * Prevents fall distance from being reset during the water-state update step,
     * ensuring fall damage is not discarded before it can be applied on water entry.
     */
    @WrapOperation(method = "updateInWaterStateAndDoWaterCurrentPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;resetFallDistance()V"))
    private void insanesurvivaloverhaul$onResetFallDistanceInUpdateInWaterState(Entity instance, Operation<Void> original) {
        if (Fluids.shouldOverrideWaterFallDamageModifier())
            return;
        original.call(instance);
    }
}
