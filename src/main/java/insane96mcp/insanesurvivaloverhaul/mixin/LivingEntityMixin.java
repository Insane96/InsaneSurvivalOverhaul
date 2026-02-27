package insane96mcp.insanesurvivaloverhaul.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanesurvivaloverhaul.module.combat.RegeneratingAbsorption;
import insane96mcp.insanesurvivaloverhaul.module.combat.Shields;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    // --- Shields ---

    @ModifyExpressionValue(method = "isBlocking", at = @At(value = "CONSTANT", args = "intValue=5"))
    private int shieldsPlus$blockingWindupTime(int ticks) {
        return Shields.getShieldWindUp(ticks);
    }

    // --- Regenerating Absorption ---

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    public void onPlayHurtSound(LivingEntity instance, DamageSource pSource, Operation<Void> original) {
        if (ModNBTData.contains(instance, RegeneratingAbsorption.NO_HURT_SOUND_TAG))
            return;
        original.call(instance, pSource);
    }

    @WrapOperation(method = "handleDamageEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    public void onPlayHurtSoundClientSide(LivingEntity instance, SoundEvent soundEvent, float volume, float pitch, Operation<Void> original, DamageSource source) {
        if (RegeneratingAbsorption.canDamageAbsorption(source) && ModNBTData.get(instance, RegeneratingAbsorption.REGEN_ABSORPTION_TAG, Float.class) > 0)
            return;
        original.call(instance, soundEvent, volume, pitch);
    }
}
