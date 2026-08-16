package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.conduit;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.misc.Conduit;
import net.minecraft.world.effect.MobEffectUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEffectUtil.class)
public class MobEffectUtilMixin_Conduit {

    /**
     * Prevents Conduit Power's hasEffect(CONDUIT_POWER) check from granting dig speed amplification, when configured to.
     */
    @ModifyExpressionValue(method = "getDigSpeedAmplification", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z", ordinal = 1))
    private static boolean insanesurvivaloverhaul$removeHasteFromConduit(boolean original) {
        return Conduit.shouldRemoveConduitHaste() ? false : original;
    }

    /**
     * Prevents Conduit Power's hasEffect(CONDUIT_POWER) check from making {@code hasDigSpeed} report true, when configured to.
     */
    @ModifyExpressionValue(method = "hasDigSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z", ordinal = 1))
    private static boolean insanesurvivaloverhaul$removeHasteFromConduitClient(boolean original) {
        return Conduit.shouldRemoveConduitHaste() ? false : original;
    }
}
