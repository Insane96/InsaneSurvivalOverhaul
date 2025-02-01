package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.combat.MiscStats;
import insane96mcp.iguanatweaksreborn.module.combat.bows.Bows;
import insane96mcp.iguanatweaksreborn.module.combat.bows.ShortbowItem;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BowItem.class)
public class BowItemMixin {
    @ModifyArg(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;setBaseDamage(D)V"))
    public double iguanatweaksreborn$setBaseDamage(double pBaseDamage, @Local AbstractArrow abstractArrow, @Local(ordinal = 2) int powerLvl) {
        if (!EnchantmentsFeature.powerAffectsBaseArrowDamage && EnchantmentsFeature.powerEnchantmentDamage == 0.5d)
            return pBaseDamage;
        if (EnchantmentsFeature.powerAffectsBaseArrowDamage)
            return abstractArrow.getBaseDamage() + (abstractArrow.getBaseDamage() * EnchantmentsFeature.powerEnchantmentDamage * powerLvl);
        else
            return abstractArrow.getBaseDamage() + EnchantmentsFeature.powerEnchantmentDamage + EnchantmentsFeature.powerEnchantmentDamage * powerLvl;
    }

    @ModifyExpressionValue(method = "releaseUsing", at = @At(value = "CONSTANT", args = "floatValue=1.0", ordinal = 0))
    public float iguanatweaksreborn$shootInaccuracy(float original) {
        if (!Feature.isEnabled(MiscStats.class))
            return original;
        return Bows.bowInaccuracy.floatValue();
    }

    @WrapOperation(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F"))
    public float iguanatweaksreborn$shortBowPower(int chargeTicks, Operation<Float> original, ItemStack stack) {
        if (!stack.is(Bows.SHORTBOW.get()))
            return original.call(chargeTicks);
        return ShortbowItem.getPowerForTime(chargeTicks);
    }
}
