package insane96mcp.insanesurvivaloverhaul.mixin.module.client;

import insane96mcp.insanesurvivaloverhaul.module.client.WorldBorder;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin_WorldBorder {

    /**
     * Caps and scales the world border render height based on {@link WorldBorder#capHeight}
     * and {@link WorldBorder#heightMultiplier} when shortening is enabled.
     */
    @ModifyVariable(at = @At(value = "STORE"), method = "renderWorldBorder", ordinal = 4)
    private double insanesurvivaloverhaul$worldBorderHeight(double value) {
        if (WorldBorder.shouldShorten())
            return Math.min(WorldBorder.capHeight, value * WorldBorder.heightMultiplier);
        return value;
    }

    /**
     * Scales the world border wall transparency by {@link WorldBorder#getTransparencyMultiplier()}.
     */
    @ModifyVariable(at = @At(value = "STORE", ordinal = 2), method = "renderWorldBorder", ordinal = 1)
    private double insanesurvivaloverhaul$worldBorderAlpha(double value) {
        return value * WorldBorder.getTransparencyMultiplier();
    }
}
