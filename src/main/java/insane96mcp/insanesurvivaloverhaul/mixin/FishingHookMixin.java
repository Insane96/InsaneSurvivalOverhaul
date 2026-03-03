package insane96mcp.insanesurvivaloverhaul.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.event.HookTickToHookLureEvent;
import insane96mcp.insanesurvivaloverhaul.event.ISOEventHook;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FishingHook.class)
public class FishingHookMixin {

    @Definition(id = "nextInt", method = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I")
    @Definition(id = "random", field = "Lnet/minecraft/world/entity/projectile/FishingHook;random:Lnet/minecraft/util/RandomSource;")
    @Expression("nextInt(this.random, 20, 80)")
    @ModifyExpressionValue(method = "catchingFish", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public int iguanatweaksreborn$hookingFishEvent(int original) {
        return ISOEventHook.onHookTickToHookLure((FishingHook) (Object) this, original, HookTickToHookLureEvent.Type.HOOK);
    }

    @Definition(id = "nextInt", method = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I")
    @Definition(id = "random", field = "Lnet/minecraft/world/entity/projectile/FishingHook;random:Lnet/minecraft/util/RandomSource;")
    @Expression("nextInt(this.random, 100, 600)")
    @ModifyExpressionValue(method = "catchingFish", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public int iguanatweaksreborn$luringFishEvent(int original) {
        return ISOEventHook.onHookTickToHookLure((FishingHook) (Object) this, original, HookTickToHookLureEvent.Type.LURE);
    }
}