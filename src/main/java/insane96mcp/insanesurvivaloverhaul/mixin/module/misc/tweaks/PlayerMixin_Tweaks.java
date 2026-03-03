package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin_Tweaks {
    /**
     * Replaces the hardcoded turtle helmet water-breathing duration (200 ticks) with the
     * configured value from {@link Tweaks#turtle$helmetWaterBreathingTime} when the Tweaks feature is enabled.
     */
    @ModifyExpressionValue(method = "turtleHelmetTick", at = @At(value = "CONSTANT", args = "intValue=200"))
    public int insanesurvivaloverhaul$onTurtleHelmetTick(int original) {
        if (!Feature.isEnabled(Tweaks.class))
            return original;
        return Tweaks.turtle$helmetWaterBreathingTime;
    }
}
