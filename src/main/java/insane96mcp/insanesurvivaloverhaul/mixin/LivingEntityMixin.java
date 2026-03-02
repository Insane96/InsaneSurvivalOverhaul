package insane96mcp.insanesurvivaloverhaul.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.event.ISOEventHook;
import insane96mcp.insanesurvivaloverhaul.module.combat.AttackSpeedBasedInvincibility;
import insane96mcp.insanesurvivaloverhaul.module.combat.PiercingDamage;
import insane96mcp.insanesurvivaloverhaul.module.combat.Shields;
import insane96mcp.insanesurvivaloverhaul.module.combat.regeneratingabsorption.RegeneratingAbsorption;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Stack;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    // --- Events ---

    @Shadow
    @Nullable
    protected Stack<DamageContainer> damageContainers;

    @ModifyArg(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/damagesource/DamageContainer;setNewDamage(F)V"))
    private float insanesurvivaloverhaul$onPreAbsorpHurt(float damage) {
        return ISOEventHook.onPreAbsorpDamage(self(), this.damageContainers.peek());
    }

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

    // --- AttackSpeedBasedInvincibility ---

    @ModifyExpressionValue(method = "hurt", at = @At(value = "CONSTANT", args = "intValue=10"))
    public int insanesurvivaloverhaul$reflectInvulnerabilityFrames(int original) {
        if (!Feature.isEnabled(AttackSpeedBasedInvincibility.class))
            return original;
        return self().invulnerableTime - 10;
    }

    @ModifyExpressionValue(method = "handleDamageEvent", at = @At(value = "CONSTANT", args = "intValue=20"))
    public int insanesurvivaloverhaul$reflectInvulnerabilityFramesInEvent(int original) {
        if (!Feature.isEnabled(AttackSpeedBasedInvincibility.class))
            return original;
        return self().invulnerableTime > 10 ? self().invulnerableTime : original;
    }

    @ModifyExpressionValue(method = "handleDamageEvent", at = @At(value = "CONSTANT", args = "intValue=10"))
    public int insanesurvivaloverhaul$reflectInvulnerabilityFramesInEvent2(int original) {
        if (!Feature.isEnabled(AttackSpeedBasedInvincibility.class))
            return original;
        return self().invulnerableTime > 10 ? self().invulnerableTime - 10 : original;
    }

    // --- Piercing Damage ---

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V", shift = At.Shift.AFTER), cancellable = true)
    public void insanesurvivaloverhaul$cancelOnDead(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir) {
        if (ModNBTData.contains(self(), PiercingDamage.SHOULD_STOP_HURT)) {
            cir.setReturnValue(ModNBTData.get(self(), PiercingDamage.SHOULD_STOP_HURT, Boolean.class));
            ModNBTData.remove(self(), PiercingDamage.SHOULD_STOP_HURT);
        }
    }

    public LivingEntity self() {
        return (LivingEntity) (Object) this;
    }
}
