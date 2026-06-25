package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth.exhaustion;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.exhaustion.Exhaustion;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin_Exhaustion {
    @ModifyExpressionValue(method = "checkMovementStatistics", at = @At(value = "CONSTANT", args = "floatValue=0.1"))
    public float insanesurvivaloverhaul$sprintExhaustion(float original) {
        if (!Feature.isEnabled(Exhaustion.class))
            return original;
        return Exhaustion.sprintExhaustion.floatValue();
    }
}
