package insane96mcp.insanesurvivaloverhaul.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.combat.AttackSounds;
import insane96mcp.insanesurvivaloverhaul.module.combat.MiscStats;
import insane96mcp.insanesurvivaloverhaul.module.combat.Shields;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Shadow
    public abstract void crit(Entity entityHit);

    @Shadow
    public abstract void magicCrit(Entity entityHit);

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    // --- Shields ---

    @ModifyExpressionValue(method = "hurtCurrentlyUsedShield", at = @At(value = "CONSTANT", args = "floatValue=3.0"))
    private float shieldsPlus$blockingWindupTime(float minDamage) {
        return Shields.getMinHurtDamage(minDamage);
    }

    // --- Attack Sounds ---

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

    // --- Misc Stats' Sweeping Overhaul ---

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V", ordinal = 1, shift = At.Shift.AFTER))
    public void iguanatweaksreborn$changeSweepingDamage(Entity pTarget, CallbackInfo ci, @Local(ordinal = 0) float f, @Local(ordinal = 5) LocalFloatRef f3) {
        if (!MiscStats.sweepingOverhaul
                || !Feature.isEnabled(MiscStats.class))
            return;
        f3.set(f);
    }

    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getSweepHitBox(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/AABB;"))
    public AABB iguanatweaksreborn$changeSweepingHitbox(AABB original) {
        if (!MiscStats.sweepingOverhaul
                || !Feature.isEnabled(MiscStats.class))
            return original;
        return original.inflate(1.5f, 0.15f, 1.5f);
    }

    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;entityInteractionRange()D"))
    public double iguanatweaksreborn$increaseSweepingReach(double original) {
        if (!MiscStats.sweepingOverhaul
                || !Feature.isEnabled(MiscStats.class))
            return original;
        return original + 1f;
    }

    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;onGround()Z", ordinal = 1))
    public boolean iguanatweaksreborn$allowSweepOffGround(boolean original) {
        if (!MiscStats.sweepingOverhaul
                || !Feature.isEnabled(MiscStats.class))
            return original;
        return true;
    }

    @Definition(id = "flag", local = @Local(type = boolean.class, ordinal = 1))
    @Expression("flag")
    @ModifyExpressionValue(method = "attack", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
    public boolean iguanatweaksreborn$allowSweepWhenSprinting(boolean original) {
        if (!MiscStats.sweepingOverhaul
                || !Feature.isEnabled(MiscStats.class))
            return original;
        return false;
    }

    @Definition(id = "d0", local = @Local(type = double.class))
    @Definition(id = "getSpeed", method = "Lnet/minecraft/world/entity/player/Player;getSpeed()F")
    @Expression("d0 < (double) this.getSpeed()")
    @ModifyExpressionValue(method = "attack", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public boolean iguanatweaksreborn$allowSweepWhenTooFast(boolean original) {
        if (!MiscStats.sweepingOverhaul
                || !Feature.isEnabled(MiscStats.class))
            return original;
        return true;
    }

    //TODO Full damage

    /*@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", shift = At.Shift.AFTER))
    public void iguanatweaksreborn$onSweepHurt(Entity pTarget, CallbackInfo ci, @Local Vec3 vec3, @Local(name = "flag1") boolean flag1, @Local(name = "flag2") boolean flag2, @Local(name = "flag4") boolean flag4, @Local(name = "f1") float f1, @Local LivingEntity sweepTarget, @Local DamageSource damageSource) {
        if (sweepTarget instanceof ServerPlayer && sweepTarget.hurtMarked) {
            ((ServerPlayer)sweepTarget).connection.send(new ClientboundSetEntityMotionPacket(sweepTarget));
            sweepTarget.hurtMarked = false;
            sweepTarget.setDeltaMovement(vec3);
        }

        if (flag1) {
            this.level()
                    .playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, this.getSoundSource(), 1.0F, 1.0F);
            this.crit(sweepTarget);
        }

        if (!flag1 && !flag2) {
            if (flag4) {
                this.level()
                        .playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, this.getSoundSource(), 1.0F, 1.0F);
            } else {
                this.level()
                        .playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, this.getSoundSource(), 1.0F, 1.0F);
            }
        }

        if (f1 > 0.0F) {
            this.magicCrit(sweepTarget);
        }

        this.setLastHurtMob(sweepTarget);

        if (this.level() instanceof ServerLevel serverLevel)
            EnchantmentHelper.doPostAttackEffects(serverLevel, sweepTarget, damageSource);
    }*/

	/*@ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/PlayerStats;onGround()Z", ordinal = 1))
	public boolean allowSweepingOffGround(boolean original) {
		return true;
	}*/

    @ModifyExpressionValue(method = "attack", at = @At(value = "CONSTANT", args = "doubleValue=0.4000000059604645"))
    public double onSweepKnockbackStrength(double original, @Local(name = "f4") float knockbackStrength) {
        if (!MiscStats.sweepingOverhaul
                || !Feature.isEnabled(MiscStats.class))
            return original;
        return knockbackStrength * 0.5F;
    }
}
