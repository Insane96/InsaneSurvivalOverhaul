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
     * Replaces the hardcoded fall distance (0.5 blocks) that knocks shoulder entities (e.g. parrots)
     * off the player with the configured value from {@link Tweaks#parrot$shoulderDismountFallDistance}
     * when the Tweaks feature is enabled.
     */
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=0.5"))
    public float insanesurvivaloverhaul$onShoulderEntityFallDistance(float original) {
        if (!Feature.isEnabled(Tweaks.class))
            return original;
        return Tweaks.parrot$shoulderDismountFallDistance.floatValue();
    }
}
