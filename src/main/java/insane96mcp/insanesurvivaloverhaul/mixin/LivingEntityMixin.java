package insane96mcp.insanesurvivaloverhaul.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.combat.Shields;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyExpressionValue(method = "isBlocking", at = @At(value = "CONSTANT", args = "intValue=5"))
    private int shieldsPlus$blockingWindupTime(int ticks) {
        return Shields.getShieldWindUp(ticks);
    }
}
