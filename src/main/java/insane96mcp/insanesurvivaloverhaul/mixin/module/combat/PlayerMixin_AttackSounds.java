package insane96mcp.insanesurvivaloverhaul.mixin.module.combat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanesurvivaloverhaul.module.combat.AttackSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin_AttackSounds {

    /**
     * Intercepts the sweep attack sound and routes it through {@link AttackSounds#playAttackSound}.
     */
    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", ordinal = 1))
    public void insanesurvivaloverhaul$playSweepAttackSound(Level level, Player player, double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, Operation<Void> original) {
        AttackSounds.playAttackSound(level, player, x, y, z, soundEvent, soundSource, volume, pitch, original, (Player) (Object) this);
    }

    /**
     * Intercepts the crit attack sound and routes it through {@link AttackSounds#playAttackSound}.
     */
    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", ordinal = 2))
    public void insanesurvivaloverhaul$playCritAttackSound(Level level, Player player, double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, Operation<Void> original) {
        AttackSounds.playAttackSound(level, player, x, y, z, soundEvent, soundSource, volume, pitch, original, (Player) (Object) this);
    }

    /**
     * Intercepts the strong (fully-charged) attack sound and routes it through {@link AttackSounds#playAttackSound}.
     */
    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", ordinal = 3))
    public void insanesurvivaloverhaul$playStrongAttackSound(Level level, Player player, double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, Operation<Void> original) {
        AttackSounds.playAttackSound(level, player, x, y, z, soundEvent, soundSource, volume, pitch, original, (Player) (Object) this);
    }

    /**
     * Intercepts the weak (poorly-charged) attack sound and routes it through {@link AttackSounds#playAttackSound}.
     */
    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", ordinal = 4))
    public void insanesurvivaloverhaul$playWeakAttackSound(Level level, Player player, double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, Operation<Void> original) {
        AttackSounds.playAttackSound(level, player, x, y, z, soundEvent, soundSource, volume, pitch, original, (Player) (Object) this);
    }
}
