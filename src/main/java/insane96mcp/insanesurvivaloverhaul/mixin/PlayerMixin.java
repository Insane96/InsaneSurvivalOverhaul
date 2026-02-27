package insane96mcp.insanesurvivaloverhaul.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.combat.Shields;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyExpressionValue(method = "hurtCurrentlyUsedShield", at = @At(value = "CONSTANT", args = "floatValue=3.0"))
    private float shieldsPlus$blockingWindupTime(float minDamage) {
        return Shields.getMinHurtDamage(minDamage);
    }
}
