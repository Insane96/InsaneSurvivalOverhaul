package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.misc.beaconconduit.BeaconConduit;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEffectUtil.class)
public class MobEffectUtilMixin {

	@ModifyExpressionValue(method = "getDigSpeedAmplification", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z", ordinal = 1))
    private static boolean iguanatweaksreborn$removeHasteFromConduit(boolean original) {
        if (!BeaconConduit.shouldRemoveConduitHaste())
		    return original;
        return false;
	}

    @Definition(id = "pEntity", local = @Local(type = LivingEntity.class, argsOnly = true))
    @Definition(id = "hasEffect", method = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z")
    @Definition(id = "CONDUIT_POWER", field = "Lnet/minecraft/world/effect/MobEffects;CONDUIT_POWER:Lnet/minecraft/world/effect/MobEffect;")
    @Expression("pEntity.hasEffect(CONDUIT_POWER)")
    @ModifyExpressionValue(method = "hasDigSpeed", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean iguanatweaksreborn$removeHasteFromConduitClient(boolean original) {
        if (!BeaconConduit.shouldRemoveConduitHaste())
            return original;
        return false;
    }
}
