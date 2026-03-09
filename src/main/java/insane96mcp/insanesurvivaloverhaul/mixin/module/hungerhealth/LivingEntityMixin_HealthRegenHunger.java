package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth;

import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.HealthRegenHunger;
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
        if (!Feature.isEnabled(HealthRegenHunger.class)
                || HealthRegenHunger.sprint$speedPenaltyBelowHunger == 0f)
            return;
        attributeInstance.removeModifier(HealthRegenHunger.SPRINT_PENALTY_ID);
    }

    @Inject(method = "setSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;addTransientModifier(Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V"))
    public void onAddSprintingModifier(boolean pSprinting, CallbackInfo ci, @Local AttributeInstance attributeInstance) {
        if (!Feature.isEnabled(HealthRegenHunger.class)
                || HealthRegenHunger.sprint$speedPenaltyBelowHunger == 0f)
            return;
        if (!((LivingEntity)(Object)this instanceof Player player))
            return;
        float penalty = (HealthRegenHunger.sprint$speedPenaltyBelowHunger - player.getFoodData().getFoodLevel()) * HealthRegenHunger.sprint$speedReductionEachHunger.floatValue();
        if (penalty <= 0f)
            return;
        attributeInstance.addTransientModifier(new AttributeModifier(HealthRegenHunger.SPRINT_PENALTY_ID, -penalty, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
}
