package insane96mcp.insanesurvivaloverhaul.mixin.module.combat;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.combat.ArmorRework;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CombatRules.class)
public class CombatRulesMixin_ArmorRework {

    /**
     * Replaces the vanilla armor damage reduction formula with the ISO reworked one.
     * Returns early (no override) when the reworked formula returns -1, which signals
     * the vanilla formula should be used instead.
     * @see ArmorRework#getCalculatedDamage(float, float, float)
     */
    @Inject(method = "getDamageAfterAbsorb", at = @At("HEAD"), cancellable = true)
    private static void insanesurvivaloverhaul$armorReworkDamageCalculation(LivingEntity entity, float damage, DamageSource damageSource, float armorValue, float armorToughness, CallbackInfoReturnable<Float> cir) {
        if (!Feature.isEnabled(ArmorRework.class))
            return;
        float calculatedDamage = ArmorRework.getCalculatedDamage(damage, armorValue, armorToughness);
        if (calculatedDamage == -1)
            return;
        cir.setReturnValue(calculatedDamage);
    }
}
