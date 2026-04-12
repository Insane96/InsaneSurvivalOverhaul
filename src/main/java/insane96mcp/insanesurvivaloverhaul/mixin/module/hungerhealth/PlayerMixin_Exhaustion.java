package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.exhaustion.Exhaustion;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin_Exhaustion {
    @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "CONSTANT", args = "floatValue=0.05"))
    public float insanesurvivaloverhaul$jumpExhaustion(float original) {
        if (!Feature.isEnabled(Exhaustion.class))
            return original;
        return Exhaustion.jumpExhaustion.floatValue();
    }
    @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "CONSTANT", args = "floatValue=0.2"))
    public float insanesurvivaloverhaul$jumpExhaustionSprint(float original) {
        if (!Feature.isEnabled(Exhaustion.class))
            return original;
        return Exhaustion.jumpExhaustionSprint.floatValue();
    }
}
