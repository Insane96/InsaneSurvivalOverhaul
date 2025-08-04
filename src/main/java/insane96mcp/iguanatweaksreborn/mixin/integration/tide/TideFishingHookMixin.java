package insane96mcp.iguanatweaksreborn.mixin.integration.tide;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.iguanatweaksreborn.event.ISOEventFactory;
import insane96mcp.iguanatweaksreborn.event.TideHookTickToHookLureEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TideFishingHook.class)
public abstract class TideFishingHookMixin extends Projectile {
    @Shadow private int timeUntilHooked;

    protected TideFishingHookMixin(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Definition(id = "nextInt", method = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I")
    @Definition(id = "random", field = "Lcom/li64/tide/registries/entities/misc/fishing/TideFishingHook;random:Lnet/minecraft/util/RandomSource;")
    @Expression("nextInt(this.random, 20, 80)")
    @ModifyExpressionValue(method = "catchingFish", at = @At(value = "MIXINEXTRAS:EXPRESSION"), remap = false)
    public int iguanatweaksreborn$hookingFishEvent(int original) {
        return ISOEventFactory.onTideHookTickToHookLure((TideFishingHook) (Object) this, original, TideHookTickToHookLureEvent.Type.HOOK);
    }

    @Definition(id = "nextInt", method = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I")
    @Definition(id = "random", field = "Lcom/li64/tide/registries/entities/misc/fishing/TideFishingHook;random:Lnet/minecraft/util/RandomSource;")
    @Expression("nextInt(this.random, 200, 600)")
    @ModifyExpressionValue(method = "catchingFish", at = @At(value = "MIXINEXTRAS:EXPRESSION"), remap = false)
    public int iguanatweaksreborn$luringFishEvent(int original) {
        return ISOEventFactory.onTideHookTickToHookLure((TideFishingHook) (Object) this, original, TideHookTickToHookLureEvent.Type.LURE);
    }
}
