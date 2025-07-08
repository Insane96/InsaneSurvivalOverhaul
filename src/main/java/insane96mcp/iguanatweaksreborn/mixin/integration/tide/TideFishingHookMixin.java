package insane96mcp.iguanatweaksreborn.mixin.integration.tide;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
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

    @Definition(id = "timeUntilHooked", field = "Lcom/li64/tide/registries/entities/misc/fishing/TideFishingHook;timeUntilHooked:I")
    @Definition(id = "i", local = @Local(type = int.class, ordinal = 0))
    @Expression("?.timeUntilHooked = ?.timeUntilHooked - @(i)")
    @ModifyExpressionValue(method = "catchingFish", at = @At(value = "MIXINEXTRAS:EXPRESSION"), remap = false)
    public int iguanatweaksreborn$hookingFishEvent(int i) {
        return ISOEventFactory.onTideHookTickToHookLure((TideFishingHook) (Object) this, i, TideHookTickToHookLureEvent.Type.HOOK);
    }

    @Definition(id = "timeUntilLured", field = "Lcom/li64/tide/registries/entities/misc/fishing/TideFishingHook;timeUntilLured:I")
    @Definition(id = "i", local = @Local(type = int.class, ordinal = 0))
    @Expression("?.timeUntilLured = ?.timeUntilLured - @(i)")
    @ModifyExpressionValue(method = "catchingFish", at = @At(value = "MIXINEXTRAS:EXPRESSION"), remap = false)
    public int iguanatweaksreborn$luringFishEvent(int i) {
        return ISOEventFactory.onTideHookTickToHookLure((TideFishingHook) (Object) this, i, TideHookTickToHookLureEvent.Type.LURE);
    }
}
