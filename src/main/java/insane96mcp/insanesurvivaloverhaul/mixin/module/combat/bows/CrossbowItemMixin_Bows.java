package insane96mcp.insanesurvivaloverhaul.mixin.module.combat.bows;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.Bows;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin_Bows {

    /**
     * Replaces the crossbow projectile velocity constant with the configured value.
     * @see Bows#getCrossbowVelocity()
     */
    @ModifyExpressionValue(method = "getShootingPower", at = @At(value = "CONSTANT", args = "floatValue=3.15"))
    private static float insanesurvivaloverhaul$crossbowVelocity(float original) {
        return Bows.getCrossbowVelocity();
    }

    /**
     * Replaces the crossbow inaccuracy constant with the configured value.
     * @see Bows#getCrossbowInaccuracy(float)
     */
    @ModifyExpressionValue(method = "use", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
    private float insanesurvivaloverhaul$crossbowInaccuracy(float original) {
        return Bows.getCrossbowInaccuracy(original);
    }
}
