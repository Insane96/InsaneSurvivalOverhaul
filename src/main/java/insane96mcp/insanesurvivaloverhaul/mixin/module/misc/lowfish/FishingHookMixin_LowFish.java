package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.lowfish;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.event.HookTickToHookLureEvent;
import insane96mcp.insanesurvivaloverhaul.event.ISOEventHook;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FishingHook.class)
public class FishingHookMixin_LowFish {

    /**
     * Fires {@link HookTickToHookLureEvent} for the hook wait time (20–80 ticks),
     * allowing LowFish to scale it based on fishing conditions.
     */
    @Definition(id = "nextInt", method = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I")
    @Definition(id = "random", field = "Lnet/minecraft/world/entity/projectile/FishingHook;random:Lnet/minecraft/util/RandomSource;")
    @Expression("nextInt(this.random, 20, 80)")
    @ModifyExpressionValue(method = "catchingFish", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public int insanesurvivaloverhaul$hookWaitTime(int original) {
        return ISOEventHook.onHookTickToHookLure((FishingHook) (Object) this, original, HookTickToHookLureEvent.Type.HOOK);
    }

    /**
     * Fires {@link HookTickToHookLureEvent} for the lure wait time (100–600 ticks),
     * allowing LowFish to scale it based on fishing conditions.
     */
    @Definition(id = "nextInt", method = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I")
    @Definition(id = "random", field = "Lnet/minecraft/world/entity/projectile/FishingHook;random:Lnet/minecraft/util/RandomSource;")
    @Expression("nextInt(this.random, 100, 600)")
    @ModifyExpressionValue(method = "catchingFish", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public int insanesurvivaloverhaul$lureWaitTime(int original) {
        return ISOEventHook.onHookTickToHookLure((FishingHook) (Object) this, original, HookTickToHookLureEvent.Type.LURE);
    }
}
