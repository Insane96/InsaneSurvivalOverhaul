package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import insane96mcp.iguanatweaksreborn.module.combat.MiscStats;
import insane96mcp.iguanatweaksreborn.module.combat.Shields;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class Player2Mixin {
    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V", ordinal = 1, shift = At.Shift.AFTER))
    public void iguanatweaksreborn$changeSweepingDamage(Entity pTarget, CallbackInfo ci, @Local(ordinal = 0) float f, @Local(ordinal = 5) LocalFloatRef f3) {
        if (!MiscStats.sweepingOverhaul
                || !Feature.isEnabled(MiscStats.class))
            return;
        f3.set(f);
    }

    @ModifyExpressionValue(method = "hurtCurrentlyUsedShield", at = @At(value = "CONSTANT", args = "floatValue=3.0"))
    private float shieldsPlus$blockingWindupTime(float minDamage) {
        return Shields.getMinHurtDamage(minDamage);
    }
}
