package insane96mcp.insanesurvivaloverhaul.mixin.module.combat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.combat.Shields;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin_Shields {

    /**
     * Overrides the shield windup time (vanilla: 5 ticks) with the configured value.
     * @see Shields#getShieldWindUp(int)
     */
    @ModifyExpressionValue(method = "isBlocking", at = @At(value = "CONSTANT", args = "intValue=5"))
    private int insanesurvivaloverhaul$shieldWindupTime(int original) {
        return Shields.getShieldWindUp(original);
    }
}
