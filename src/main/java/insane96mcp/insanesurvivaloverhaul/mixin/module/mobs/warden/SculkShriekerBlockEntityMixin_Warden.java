package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs.warden;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.mobs.Warden;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SculkShriekerBlockEntity.class)
public class SculkShriekerBlockEntityMixin_Warden {
    /**
     * Replaces the hardcoded warning level (4) required to summon the Warden
     * with {@link Warden#summonWarningLevel} when the Warden feature is enabled.
     */
    @ModifyExpressionValue(method = "trySummonWarden", at = @At(value = "CONSTANT", args = "intValue=4"))
    private int insanesurvivaloverhaul$changeSummonWarningLevel(int original) {
        return Feature.isEnabled(Warden.class) ? Warden.summonWarningLevel : original;
    }
}
