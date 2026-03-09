package insane96mcp.insanesurvivaloverhaul.mixin.module.combat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.combat.attackspeedbasedinvincibility.AttackSpeedBasedInvincibility;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin_AttackSpeedBasedInvincibility {

    /**
     * Replaces the hardcoded 10-tick invincibility threshold in {@code hurt} with the entity's actual
     * {@code invulnerableTime - 10}, so that rapid attacks respect the scaled invincibility window.
     */
    @ModifyExpressionValue(method = "hurt", at = @At(value = "CONSTANT", args = "intValue=10"))
    public int insanesurvivaloverhaul$scaledInvincibilityFramesHurt(int original) {
        if (!Feature.isEnabled(AttackSpeedBasedInvincibility.class))
            return original;
        return ((LivingEntity) (Object) this).invulnerableTime - 10;
    }

    /**
     * Replaces the hardcoded 20-tick invincibility window in {@code handleDamageEvent} with the entity's
     * actual {@code invulnerableTime}, preserving the correct hurt flash duration.
     */
    @ModifyExpressionValue(method = "handleDamageEvent", at = @At(value = "CONSTANT", args = "intValue=20"))
    public int insanesurvivaloverhaul$scaledInvincibilityFramesEvent(int original) {
        if (!Feature.isEnabled(AttackSpeedBasedInvincibility.class))
            return original;
        LivingEntity self = (LivingEntity) (Object) this;
        return self.invulnerableTime > 10 ? self.invulnerableTime : original;
    }

    /**
     * Replaces the hardcoded 10-tick flash offset in {@code handleDamageEvent} with
     * {@code invulnerableTime - 10}, keeping the hurt flash centred in the scaled window.
     */
    @ModifyExpressionValue(method = "handleDamageEvent", at = @At(value = "CONSTANT", args = "intValue=10"))
    public int insanesurvivaloverhaul$scaledInvincibilityFramesEventOffset(int original) {
        if (!Feature.isEnabled(AttackSpeedBasedInvincibility.class))
            return original;
        LivingEntity self = (LivingEntity) (Object) this;
        return self.invulnerableTime > 10 ? self.invulnerableTime - 10 : original;
    }
}
