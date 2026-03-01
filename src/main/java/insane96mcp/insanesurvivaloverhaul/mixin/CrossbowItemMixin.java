package insane96mcp.insanesurvivaloverhaul.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.Bows;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @ModifyExpressionValue(method = "getShootingPower", at = @At(value = "CONSTANT", args = "floatValue=3.15"))
    private static float insanesurvivaloverhaul$setCrossbowVelocity(float original) {
        return Bows.getCrossbowVelocity();
    }

    @ModifyExpressionValue(method = "use", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
    private float insanesurvivaloverhaul$inaccuracy(float original) {
        return Bows.getCrossbowInaccuracy(original);
    }
}
