package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.iguanatweaksreborn.module.combat.MiscStats;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @ModifyExpressionValue(method = "getShootingPower", at = @At(value = "CONSTANT", args = "floatValue=3.15"))
    private static float powerBonusFlat(float original) {
        return MiscStats.crossbowVelocity.floatValue();
    }
}
