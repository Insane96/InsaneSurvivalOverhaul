package insane96mcp.insanesurvivaloverhaul.mixin.module.client;

import insane96mcp.insanesurvivaloverhaul.module.client.Light;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin_Light {

    /**
     * Disables the vanilla Night Vision fade-out flash and replaces it with a smooth linear
     * fade controlled by {@link Light#nightVisionFadeOutTime}.
     */
    @SuppressWarnings("ConstantConditions")
    @Inject(at = @At("RETURN"), method = "getNightVisionScale", cancellable = true)
    private static void insanesurvivaloverhaul$smoothNightVisionFade(LivingEntity livingEntity, float partialTicks, CallbackInfoReturnable<Float> callback) {
        if (!Light.shouldDisableNightVisionFlashing())
            return;
        int duration = livingEntity.getEffect(MobEffects.NIGHT_VISION).getDuration();
        callback.setReturnValue(duration > Light.nightVisionFadeOutTime || duration == -1
                ? 1.0f
                : ((float) duration - partialTicks) * (1f / Light.nightVisionFadeOutTime));
    }
}
