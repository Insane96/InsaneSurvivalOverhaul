package insane96mcp.insanesurvivaloverhaul.mixin.module.combat;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.combat.MiscCombat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin_MiscCombat {

    // --- No Damage When Spamming ---

    /**
     * When enabled, replaces the 0.2 attack power threshold with 0 so that any attack
     * below full charge deals no damage at all.
     * @see MiscCombat#noDamageWhenSpamming()
     */
    @ModifyExpressionValue(method = "attack", at = @At(value = "CONSTANT", args = "floatValue=0.2", ordinal = 0))
    public float insanesurvivaloverhaul$noDamageWhenSpammingThreshold(float value) {
        return MiscCombat.noDamageWhenSpamming() ? 0f : value;
    }

    /**
     * When enabled, replaces the 0.8 attack power multiplier with 1 so that only fully
     * charged attacks deal full damage (no partial scaling).
     * @see MiscCombat#noDamageWhenSpamming()
     */
    @ModifyExpressionValue(method = "attack", at = @At(value = "CONSTANT", args = "floatValue=0.8"))
    public float insanesurvivaloverhaul$noDamageWhenSpammingMultiplier(float value) {
        return MiscCombat.noDamageWhenSpamming() ? 1f : value;
    }

    // --- Sweeping Overhaul ---

    /**
     * When sweeping overhaul is enabled, sets the sweep damage equal to the full attack damage
     * instead of the vanilla reduced fraction.
     */
    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V", ordinal = 1, shift = At.Shift.AFTER))
    public void insanesurvivaloverhaul$sweepingFullDamage(Entity pTarget, CallbackInfo ci, @Local(ordinal = 0) float f, @Local(ordinal = 5) LocalFloatRef f3) {
        if (!MiscCombat.sweepingOverhaul || !Feature.isEnabled(MiscCombat.class))
            return;
        f3.set(f);
    }

    /**
     * When sweeping overhaul is enabled, inflates the sweep hit box by 1.5 blocks on each
     * horizontal axis and 0.15 blocks vertically for a wider sweep arc.
     */
    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getSweepHitBox(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/AABB;"))
    public AABB insanesurvivaloverhaul$sweepingHitbox(AABB original) {
        if (!MiscCombat.sweepingOverhaul || !Feature.isEnabled(MiscCombat.class))
            return original;
        return original.inflate(1.5f, 0.15f, 1.5f);
    }

    /**
     * When sweeping overhaul is enabled, extends the sweep reach by 1 block.
     */
    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;entityInteractionRange()D"))
    public double insanesurvivaloverhaul$sweepingReach(double original) {
        if (!MiscCombat.sweepingOverhaul || !Feature.isEnabled(MiscCombat.class))
            return original;
        return original + 1f;
    }

    /**
     * When sweeping overhaul is enabled, allows sweeping while airborne.
     */
    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;onGround()Z", ordinal = 1))
    public boolean insanesurvivaloverhaul$sweepingOffGround(boolean original) {
        if (!MiscCombat.sweepingOverhaul || !Feature.isEnabled(MiscCombat.class))
            return original;
        return true;
    }

    /**
     * When sweeping overhaul is enabled, allows sweeping while sprinting.
     */
    @Definition(id = "flag", local = @Local(type = boolean.class, ordinal = 1))
    @Expression("flag")
    @ModifyExpressionValue(method = "attack", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
    public boolean insanesurvivaloverhaul$sweepingWhileSprinting(boolean original) {
        if (!MiscCombat.sweepingOverhaul || !Feature.isEnabled(MiscCombat.class))
            return original;
        return false;
    }

    /**
     * When sweeping overhaul is enabled, allows sweeping while moving too fast.
     */
    @Definition(id = "d0", local = @Local(type = double.class))
    @Definition(id = "getSpeed", method = "Lnet/minecraft/world/entity/player/Player;getSpeed()F")
    @Expression("d0 < (double) this.getSpeed()")
    @ModifyExpressionValue(method = "attack", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public boolean insanesurvivaloverhaul$sweepingWhileFast(boolean original) {
        if (!MiscCombat.sweepingOverhaul || !Feature.isEnabled(MiscCombat.class))
            return original;
        return true;
    }

    /**
     * When sweeping overhaul is enabled, scales the sweep knockback strength proportionally
     * to the attack's knockback level instead of using the hardcoded 0.4 constant.
     */
    @ModifyExpressionValue(method = "attack", at = @At(value = "CONSTANT", args = "doubleValue=0.4000000059604645"))
    public double insanesurvivaloverhaul$sweepingKnockbackStrength(double original, @Local(name = "f4") float knockbackStrength) {
        if (!MiscCombat.sweepingOverhaul || !Feature.isEnabled(MiscCombat.class))
            return original;
        return knockbackStrength * 0.5F;
    }
}
