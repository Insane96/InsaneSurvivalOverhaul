package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.turtles;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.misc.turtles.Turtles;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin_Turtles {
    /**
     * Replaces the hardcoded turtle helmet water-breathing duration (200 ticks) with the
     * configured value from {@link Turtles#helmetWaterBreathingTime} when the Turtles feature is enabled.
     */
    @ModifyExpressionValue(method = "turtleHelmetTick", at = @At(value = "CONSTANT", args = "intValue=200"))
    public int insanesurvivaloverhaul$onTurtleHelmetTick(int original) {
        if (!Feature.isEnabled(Turtles.class))
            return original;
        return Turtles.helmetWaterBreathingTime;
    }
}
