package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.entity.PathfinderMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PathfinderMob.class)
public abstract class PathfinderMobMixin {
    @ModifyExpressionValue(method = "tickLeash", at = @At(value = "CONSTANT", args = "floatValue=10.0"))
    private float iguanatweaksreborn$changeMaxLeashDistance(float original) {
        return Feature.isEnabled(Tweaks.class) ? Tweaks.leashMaxDistance.floatValue() : original;
    }
}