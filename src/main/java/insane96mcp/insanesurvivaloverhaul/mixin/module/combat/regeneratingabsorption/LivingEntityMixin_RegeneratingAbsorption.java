package insane96mcp.insanesurvivaloverhaul.mixin.module.combat.regeneratingabsorption;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanesurvivaloverhaul.event.ISOEventHook;
import insane96mcp.insanesurvivaloverhaul.module.combat.regeneratingabsorption.RegeneratingAbsorption;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import javax.annotation.Nullable;
import java.util.Stack;

@Mixin(LivingEntity.class)
public class LivingEntityMixin_RegeneratingAbsorption {

    @Shadow
    @Nullable
    protected Stack<DamageContainer> damageContainers;

    /**
     * Fires {@link insane96mcp.insanesurvivaloverhaul.event.LivingDamageEventPreAbsorp} just before
     * absorption is applied, allowing RegeneratingAbsorption to intercept and consume the damage first.
     */
    @ModifyArg(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/damagesource/DamageContainer;setNewDamage(F)V"))
    private float insanesurvivaloverhaul$firePreAbsorpEvent(float damage) {
        return ISOEventHook.onPreAbsorpDamage(self(), this.damageContainers.peek());
    }

    /**
     * Suppresses the hurt sound when {@link RegeneratingAbsorption#NO_HURT_SOUND_TAG} is set,
     * used to silence sounds when regenerating absorption shields absorb the hit instead of health.
     */
    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    public void insanesurvivaloverhaul$suppressHurtSound(LivingEntity instance, DamageSource pSource, Operation<Void> original) {
        if (ModNBTData.contains(instance, RegeneratingAbsorption.NO_HURT_SOUND_TAG))
            return;
        original.call(instance, pSource);
    }

    /**
     * Suppresses the client-side hurt sound when regenerating absorption is active and can absorb the damage.
     */
    @WrapOperation(method = "handleDamageEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    public void insanesurvivaloverhaul$suppressHurtSoundClientSide(LivingEntity instance, SoundEvent soundEvent, float volume, float pitch, Operation<Void> original, DamageSource source) {
        if (RegeneratingAbsorption.canDamageAbsorption(source) && ModNBTData.get(instance, RegeneratingAbsorption.REGEN_ABSORPTION_TAG, Float.class) > 0)
            return;
        original.call(instance, soundEvent, volume, pitch);
    }

    private LivingEntity self() {
        return (LivingEntity) (Object) this;
    }
}
