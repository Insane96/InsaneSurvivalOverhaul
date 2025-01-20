package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.combat.ArrowStats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @ModifyExpressionValue(method = "getShootingPower", at = @At(value = "CONSTANT", args = "floatValue=3.15"))
    private static float powerBonusFlat(float original) {
        return ArrowStats.getCrossbowVelocity();
    }

    @Inject(method = "getArrow", at = @At(value = "RETURN"))
    private static void iguanatweaksreborn$onSetPiercing(Level pLevel, LivingEntity pLivingEntity, ItemStack pCrossbowStack, ItemStack pAmmoStack, CallbackInfoReturnable<AbstractArrow> cir, @Local AbstractArrow abstractArrow) {
        ArrowStats.piercingCrossbows(abstractArrow);
    }
}
