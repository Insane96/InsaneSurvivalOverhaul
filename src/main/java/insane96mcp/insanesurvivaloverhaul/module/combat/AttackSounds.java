package insane96mcp.insanesurvivaloverhaul.module.combat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.COMBAT,
        description = "Changes attack sound based off tool used. Axes use vanilla strong attack, swords use vanilla weak attack, pickaxes and shovels use new sounds.")
public class AttackSounds extends Feature {
    public static final DeferredHolder<SoundEvent, SoundEvent> PICKAXE_ATTACK = ISORegistries.SOUND_EVENTS.register("pickaxe_attack", () -> SoundEvent.createFixedRangeEvent(InsaneSO.id("pickaxe_attack"), 16f));
    public static final DeferredHolder<SoundEvent, SoundEvent> SHOVEL_ATTACK = ISORegistries.SOUND_EVENTS.register("shovel_attack", () -> SoundEvent.createFixedRangeEvent(InsaneSO.id("shovel_attack"), 16f));

    @Config
    public static Boolean randomizePitch = true;

    public static void playAttackSound(Level level, Player player, double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, Operation<SoundEvent> original, Player attackingPlayer) {
        if (!Feature.isEnabled(AttackSounds.class)) {
            original.call(level, player, x, y, z, soundEvent, soundSource, volume, pitch);
            return;
        }

        if (randomizePitch)
            pitch = (float) (Math.random() * 0.4 + 0.8);
        //If it's the sweep attack, don't play other sounds, just use the possible randomized pitch
        if (soundEvent == SoundEvents.PLAYER_ATTACK_SWEEP) {
            attackingPlayer.level().playSound(null, attackingPlayer.getX(), attackingPlayer.getY(), attackingPlayer.getZ(), soundEvent, attackingPlayer.getSoundSource(), 1.0F, pitch);
            return;
        }

        SoundEvent attackSoundEvent = soundEvent;
        boolean changedSound = false;
        if (attackingPlayer.getMainHandItem().is(ItemTags.AXES)) {
            attackSoundEvent = SoundEvents.PLAYER_ATTACK_STRONG;
            changedSound = true;
        }
        else if (attackingPlayer.getMainHandItem().is(ItemTags.SWORDS)) {
            attackSoundEvent = SoundEvents.PLAYER_ATTACK_WEAK;
            changedSound = true;
        }
        else if (attackingPlayer.getMainHandItem().is(ItemTags.PICKAXES)) {
            attackSoundEvent = PICKAXE_ATTACK.get();
            changedSound = true;
        }
        else if (attackingPlayer.getMainHandItem().is(ItemTags.SHOVELS)) {
            attackSoundEvent = SHOVEL_ATTACK.get();
            changedSound = true;
        }

        //If it's a crit, also reproduce the attack sound
        if (soundEvent == SoundEvents.PLAYER_ATTACK_CRIT)
            attackingPlayer.level().playSound(null, attackingPlayer.getX(), attackingPlayer.getY(), attackingPlayer.getZ(), soundEvent, attackingPlayer.getSoundSource(), 1.0F, pitch);
        if (changedSound)
            original.call(level, null, x, y, z, attackSoundEvent, soundSource, volume, pitch);
    }

    public enum AttackType {
        CRIT,
        STRONG,
        WEAK
    }
}
