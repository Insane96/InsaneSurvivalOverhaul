package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.combat.ArrowStats;
import insane96mcp.iguanatweaksreborn.module.combat.MiscStats;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BowItem.class)
public class BowItemMixin {
    /*@ModifyExpressionValue(method = "releaseUsing", at = @At(value = "CONSTANT", args = "doubleValue=0.5", ordinal = 0))
    public double powerBonusPerLevel(double pBaseDamage) {
        return EnchantmentsFeature.powerEnchantmentDamage;
    }

    @ModifyExpressionValue(method = "releaseUsing", at = @At(value = "CONSTANT", args = "doubleValue=0.5", ordinal = 1))
    public double powerBonusFlat(double pBaseDamage) {
        return EnchantmentsFeature.powerEnchantmentDamage != 0.5d ? 0 : pBaseDamage;
    }*/

    @ModifyArg(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;setBaseDamage(D)V"))
    public double setBaseDamage(double pBaseDamage, @Local AbstractArrow abstractArrow, @Local(ordinal = 2) int powerLvl) {
        if (!EnchantmentsFeature.powerEnchantmentMultiplier && EnchantmentsFeature.powerEnchantmentDamage == 0.5d)
            return pBaseDamage;
        if (EnchantmentsFeature.powerEnchantmentMultiplier)
            return abstractArrow.getBaseDamage() + (abstractArrow.getBaseDamage() * EnchantmentsFeature.powerEnchantmentDamage * powerLvl);
        else
            return abstractArrow.getBaseDamage() + EnchantmentsFeature.powerEnchantmentDamage + EnchantmentsFeature.powerEnchantmentDamage * powerLvl;
    }

    @ModifyExpressionValue(method = "releaseUsing", at = @At(value = "CONSTANT", args = "floatValue=1.0", ordinal = 0))
    public float shootInaccuracy(float original) {
        if (!Feature.isEnabled(MiscStats.class))
            return original;
        return ArrowStats.bowInaccuracy.floatValue();
    }
}
