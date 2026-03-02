package insane96mcp.insanesurvivaloverhaul.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.client.Light;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightTexture.class)
public class LightTextureMixin {
    @ModifyExpressionValue(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 1))
    public float onGamma(float value) {
        if (Feature.isEnabled(Light.class) && Light.forceDarkness >= 0)
            return (float) (Light.forceOnlyLowerBrightness ? Math.min(Light.forceDarkness, value) : Light.forceDarkness);
        return value;
    }
}
