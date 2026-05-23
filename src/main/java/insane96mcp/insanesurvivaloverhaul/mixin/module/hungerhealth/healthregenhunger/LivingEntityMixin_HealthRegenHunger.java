package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth.healthregenhunger;

import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.movement.Sprinting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin_HealthRegenHunger {
    @Inject(method = "setSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;removeModifier(Lnet/minecraft/resources/ResourceLocation;)Z"))
    public void onRemoveSprintingModifier(boolean pSprinting, CallbackInfo ci, @Local AttributeInstance attributeInstance) {
        if (!Feature.isEnabled(Sprinting.class)
                || Sprinting.speedPenaltyBelowHunger == 0f)
            return;
        attributeInstance.removeModifier(Sprinting.SPRINT_PENALTY_ID);
    }

    @Inject(method = "setSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;addTransientModifier(Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V"))
    public void onAddSprintingModifier(boolean pSprinting, CallbackInfo ci, @Local AttributeInstance attributeInstance) {
        if (!Feature.isEnabled(Sprinting.class)
                || Sprinting.speedPenaltyBelowHunger == 0f)
            return;
        if (!((LivingEntity)(Object)this instanceof Player player))
            return;
        float penalty = (Sprinting.speedPenaltyBelowHunger - player.getFoodData().getFoodLevel()) * Sprinting.speedReductionEachHunger.floatValue();
        if (penalty <= 0f)
            return;
        attributeInstance.addTransientModifier(new AttributeModifier(Sprinting.SPRINT_PENALTY_ID, -penalty, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
}
