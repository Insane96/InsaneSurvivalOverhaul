package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import insane96mcp.iguanatweaksreborn.module.combat.AttackSounds;
import insane96mcp.iguanatweaksreborn.module.combat.MiscStats;
import insane96mcp.iguanatweaksreborn.module.combat.Shields;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", ordinal = 1))
    public void iguanatweaksreborn$playSweepSound(Level level, Player player, double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, Operation<SoundEvent> original) {
        AttackSounds.playAttackSound(level, player, x, y, z, soundEvent, soundSource, volume, pitch, original, (Player) (Object) this);
    }

    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", ordinal = 2))
    public void iguanatweaksreborn$playCritSound(Level level, Player player, double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, Operation<SoundEvent> original) {
        AttackSounds.playAttackSound(level, player, x, y, z, soundEvent, soundSource, volume, pitch, original, (Player) (Object) this);
    }

    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", ordinal = 3))
    public void iguanatweaksreborn$playStrongSound(Level level, Player player, double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, Operation<SoundEvent> original) {
        AttackSounds.playAttackSound(level, player, x, y, z, soundEvent, soundSource, volume, pitch, original, (Player) (Object) this);
    }

    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", ordinal = 4))
    public void iguanatweaksreborn$playWeakSound(Level level, Player player, double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, Operation<SoundEvent> original) {
        AttackSounds.playAttackSound(level, player, x, y, z, soundEvent, soundSource, volume, pitch, original, (Player) (Object) this);
    }
}
