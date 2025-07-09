package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.event.HookTickToHookLureEvent;
import insane96mcp.iguanatweaksreborn.event.ISOEventFactory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Projectile {

    @Shadow private int timeUntilHooked;

    protected FishingHookMixin(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Definition(id = "timeUntilHooked", field = "Lnet/minecraft/world/entity/projectile/FishingHook;timeUntilHooked:I")
    @Definition(id = "i", local = @Local(type = int.class, ordinal = 0))
    @Expression("?.timeUntilHooked = ?.timeUntilHooked - @(i)")
    @ModifyExpressionValue(method = "catchingFish", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public int iguanatweaksreborn$hookingFishEvent(int i) {
        return ISOEventFactory.onHookTickToHookLure((FishingHook) (Object) this, i, HookTickToHookLureEvent.Type.HOOK);
    }

    @Definition(id = "timeUntilLured", field = "Lnet/minecraft/world/entity/projectile/FishingHook;timeUntilLured:I")
    @Definition(id = "i", local = @Local(type = int.class, ordinal = 0))
    @Expression("?.timeUntilLured = ?.timeUntilLured - @(i)")
    @ModifyExpressionValue(method = "catchingFish", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public int iguanatweaksreborn$luringFishEvent(int i) {
        return ISOEventFactory.onHookTickToHookLure((FishingHook) (Object) this, i, HookTickToHookLureEvent.Type.LURE);
    }
}