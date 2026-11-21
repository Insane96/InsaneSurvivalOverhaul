package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs.warden;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.mobs.Warden;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WardenSpawnTracker.class)
public class WardenSpawnTrackerMixin_Warden {
    /**
     * Replaces the hardcoded interval (12000 ticks) after which the warning level decreases
     * with {@link Warden#warningDecreaseInterval} when the Warden feature is enabled.
     */
    @ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=12000"))
    private int insanesurvivaloverhaul$changeWarningDecreaseInterval(int original) {
        return Feature.isEnabled(Warden.class) ? Warden.warningDecreaseInterval : original;
    }

    /**
     * Replaces the hardcoded cooldown (200 ticks) set after the warning level increases
     * with {@link Warden#warningCooldown} when the Warden feature is enabled.
     */
    @ModifyExpressionValue(method = "increaseWarningLevel", at = @At(value = "CONSTANT", args = "intValue=200"))
    private int insanesurvivaloverhaul$changeWarningCooldown(int original) {
        return Feature.isEnabled(Warden.class) ? Warden.warningCooldown : original;
    }
}
