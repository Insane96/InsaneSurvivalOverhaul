package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.iguanatweaksreborn.module.combat.AttackSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixinAttackSounds {
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
