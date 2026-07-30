package insane96mcp.insanesurvivaloverhaul.mixin.module.movement;

import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.movement.Sprinting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin_Sprinting {

    @Unique
    private double insanesurvivaloverhaul$preSprintMovementSpeed;

    /**
     * Removes the sprint slowdown penalty alongside vanilla's own sprint speed modifier.
     * @see Sprinting#SPRINT_SPEED_ATTRIBUTE
     */
    @Inject(method = "setSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;removeModifier(Lnet/minecraft/resources/ResourceLocation;)Z"))
    private void insanesurvivaloverhaul$removeSprintSlowdown(boolean sprinting, CallbackInfo ci, @Local(name = "attributeinstance") AttributeInstance attributeinstance) {
        if (!Feature.isEnabled(Sprinting.class))
            return;
        attributeinstance.removeModifier(Sprinting.SPRINT_SPEED_ID);
    }

    /**
     * Captures movement speed right before vanilla adds its own sprint speed modifier, i.e. the entity's
     * walking speed. Used by {@link #insanesurvivaloverhaul$addSprintSlowdown} to make sure the slowdown
     * penalty never brings sprint speed below it.
     */
    @Inject(method = "setSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;addTransientModifier(Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V"))
    private void insanesurvivaloverhaul$capturePreSprintMovementSpeed(boolean sprinting, CallbackInfo ci, @Local(name = "attributeinstance") AttributeInstance attributeinstance) {
        if (!Feature.isEnabled(Sprinting.class))
            return;
        this.insanesurvivaloverhaul$preSprintMovementSpeed = attributeinstance.getValue();
    }

    /**
     * Applies the combined sprint slowdown penalty (from armor_rework and/or low hunger) to movement_speed
     * only while sprinting.
     * @see Sprinting#SPRINT_SPEED_ATTRIBUTE
     */
    @Inject(method = "setSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;addTransientModifier(Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V", shift = At.Shift.AFTER))
    private void insanesurvivaloverhaul$addSprintSlowdown(boolean sprinting, CallbackInfo ci, @Local(name = "attributeinstance") AttributeInstance attributeinstance) {
        if (!Feature.isEnabled(Sprinting.class))
            return;
        LivingEntity self = (LivingEntity)(Object) this;
        double slowdownPercent = self.getAttributeValue(Sprinting.SPRINT_SPEED_ATTRIBUTE);
        if (slowdownPercent == 0d)
            return;
        double speedWithSprint = attributeinstance.getValue();
        if (speedWithSprint <= 0d)
            return;
        double sprintSpeed = speedWithSprint - this.insanesurvivaloverhaul$preSprintMovementSpeed;
        double sprintSpeedPercentage = sprintSpeed / this.insanesurvivaloverhaul$preSprintMovementSpeed;
        // ADD_MULTIPLIED_TOTAL modifiers multiply together onto the pre-total value, not onto each other's
        // results, so the slowdown factor must be divided by (1 + sprintSpeedPercentage) to correctly scale
        // down just the sprint bonus rather than the whole speed.
        double slowdown = sprintSpeedPercentage * slowdownPercent / (1d + sprintSpeedPercentage);
        attributeinstance.addTransientModifier(new AttributeModifier(Sprinting.SPRINT_SPEED_ID, slowdown, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
}
