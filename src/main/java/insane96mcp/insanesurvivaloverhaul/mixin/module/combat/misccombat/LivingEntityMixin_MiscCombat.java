package insane96mcp.insanesurvivaloverhaul.mixin.module.combat.misccombat;

import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.combat.MiscCombat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin_MiscCombat {

    // --- Armor Rework: sprint-only movement speed penalty ---

    /**
     * Removes the armor_rework movement speed penalty alongside vanilla's own sprint speed modifier.
     * @see MiscCombat#SPRINT_SLOWDOWN_ATTRIBUTE
     */
    @Inject(method = "setSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;removeModifier(Lnet/minecraft/resources/ResourceLocation;)Z"))
    private void insanesurvivaloverhaul$removeArmorSprintSlowdown(boolean sprinting, CallbackInfo ci, @Local(name = "attributeinstance") AttributeInstance attributeinstance) {
        if (!Feature.isEnabled(MiscCombat.class) || !MiscCombat.armorReworkDataPack)
            return;
        attributeinstance.removeModifier(MiscCombat.SPRINT_SLOWDOWN_ID);
    }

    /**
     * Applies the armor_rework movement speed penalty to movement_speed only while sprinting,
     * instead of at all times.
     * @see MiscCombat#SPRINT_SLOWDOWN_ATTRIBUTE
     */
    @Inject(method = "setSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;addTransientModifier(Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V"))
    private void insanesurvivaloverhaul$addArmorSprintSlowdown(boolean sprinting, CallbackInfo ci, @Local(name = "attributeinstance") AttributeInstance attributeinstance) {
        if (!Feature.isEnabled(MiscCombat.class) || !MiscCombat.armorReworkDataPack)
            return;
        LivingEntity self = (LivingEntity)(Object) this;
        double slowdown = self.getAttributeValue(MiscCombat.SPRINT_SLOWDOWN_ATTRIBUTE);
        if (slowdown == 0d)
            return;
        attributeinstance.addTransientModifier(new AttributeModifier(MiscCombat.SPRINT_SLOWDOWN_ID, slowdown, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }
}
