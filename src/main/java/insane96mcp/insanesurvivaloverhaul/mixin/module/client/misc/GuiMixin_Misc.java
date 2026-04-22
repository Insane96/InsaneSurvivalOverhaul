package insane96mcp.insanesurvivaloverhaul.mixin.module.client.misc;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanesurvivaloverhaul.module.client.Misc;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin_Misc {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract boolean isExperienceBarVisible();

    @Shadow
    protected abstract void renderExperienceBar(GuiGraphics guiGraphics, int x);

    // --- Floaty Hotbar ---

    /** Shifts the hotbar background sprite upward by {@link Misc#floatyHotbar} pixels. */
    @ModifyExpressionValue(method = "renderItemHotbar", at = @At(value = "CONSTANT", args = "intValue=22", ordinal = 0))
    public int insanesurvivaloverhaul$hotBarShift(int original) {
        if (!Misc.shouldRaiseHotbar()) return original;
        return original + Misc.floatyHotbar;
    }

    /** Shifts the selected-slot highlight upward by {@link Misc#floatyHotbar} pixels. */
    @ModifyExpressionValue(method = "renderItemHotbar", at = @At(value = "CONSTANT", args = "intValue=22", ordinal = 2))
    public int insanesurvivaloverhaul$selectedHotBarShift(int original) {
        if (!Misc.shouldRaiseHotbar()) return original;
        return original + Misc.floatyHotbar;
    }

    /** Vanilla has cut the selected-slot highlight texture for ... reasons. This increases back the size. Also requires the resource pack. */
    @ModifyExpressionValue(method = "renderItemHotbar", at = @At(value = "CONSTANT", args = "intValue=23", ordinal = 0))
    public int insanesurvivaloverhaul$selectedHotBarHeight(int original) {
        if (!Misc.shouldRaiseHotbar()
                || !Misc.slotSelectionResourcePack) return original;
        return original + 1;
    }

    /** Shifts the off-hand slot (left) upward by {@link Misc#floatyHotbar} pixels. */
    @ModifyExpressionValue(method = "renderItemHotbar", at = @At(value = "CONSTANT", args = "intValue=23", ordinal = 1))
    public int insanesurvivaloverhaul$offHandShiftLeft(int original) {
        if (!Misc.shouldRaiseHotbar()) return original;
        return original + Misc.floatyHotbar;
    }

    /** Shifts the off-hand slot (right) upward by {@link Misc#floatyHotbar} pixels. */
    @ModifyExpressionValue(method = "renderItemHotbar", at = @At(value = "CONSTANT", args = "intValue=23", ordinal = 2))
    public int insanesurvivaloverhaul$offHandShiftRight(int original) {
        if (!Misc.shouldRaiseHotbar()) return original;
        return original + Misc.floatyHotbar;
    }

    /** Shifts main-hand items upward by {@link Misc#floatyHotbar} pixels. */
    @ModifyExpressionValue(method = "renderItemHotbar", at = @At(value = "CONSTANT", args = "intValue=16", ordinal = 0))
    public int insanesurvivaloverhaul$renderItems(int original) {
        if (!Misc.shouldRaiseHotbar()) return original;
        return original + Misc.floatyHotbar;
    }

    /** Shifts off-hand items upward by {@link Misc#floatyHotbar} pixels. */
    @ModifyExpressionValue(method = "renderItemHotbar", at = @At(value = "CONSTANT", args = "intValue=16", ordinal = 1))
    public int insanesurvivaloverhaul$renderItemsOffhand(int original) {
        if (!Misc.shouldRaiseHotbar()) return original;
        return original + Misc.floatyHotbar;
    }

    /** Shifts the attack indicator upward by {@link Misc#floatyHotbar} pixels. */
    @ModifyExpressionValue(method = "renderItemHotbar", at = @At(value = "CONSTANT", args = "intValue=20", ordinal = 2))
    public int insanesurvivaloverhaul$renderAttackIndicator(int original) {
        if (!Misc.shouldRaiseHotbar()) return original;
        return original + Misc.floatyHotbar;
    }

    /** Shifts the experience bar and level number upward by {@link Misc#floatyHotbar} pixels. */
    @ModifyExpressionValue(method = "renderExperienceBar", at = @At(value = "CONSTANT", args = "intValue=32", ordinal = 0))
    public int insanesurvivaloverhaul$experienceBarShift(int original) {
        if (!Misc.shouldRaiseHotbar()) return original;
        return original + Misc.floatyHotbar;
    }

    /** Shifts the experience bar and level number upward by {@link Misc#floatyHotbar} pixels. */
    @ModifyExpressionValue(method = "renderExperienceLevel", at = @At(value = "CONSTANT", args = "intValue=31", ordinal = 0))
    public int insanesurvivaloverhaul$experienceLevelShift(int original) {
        if (!Misc.shouldRaiseHotbar()) return original;
        return original + Misc.floatyHotbar;
    }

    /** Shifts the jump meter upward by {@link Misc#floatyHotbar} pixels. */
    @ModifyExpressionValue(method = "renderJumpMeter", at = @At(value = "CONSTANT", args = "intValue=32", ordinal = 0))
    public int insanesurvivaloverhaul$jumpMeterShift(int original) {
        if (!Misc.shouldRaiseHotbar()) return original;
        return original + Misc.floatyHotbar;
    }

    // --- Fix Mounts GUI ---

    /**
     * Replaces the vehicle health bar heart count with the actual mount max health
     * so large-health mounts render correctly.
     */
    @ModifyVariable(method = "renderVehicleHealth", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    public int insanesurvivaloverhaul$fixMountHealthBar(int original, @Local LivingEntity mount) {
        if (!Misc.fixMountsGui())
            return original;
        return (int) (mount.getMaxHealth() + 0.5F) / 2;
    }

    /**
     * Forces the food level bar to always render while mounted, even when the mount's
     * jump bar is active, so the player's hunger is always visible.
     */
    @Definition(id = "l2", local = @Local(type = int.class))
    @Expression("l2 == 0")
    @WrapOperation(method = "renderFoodLevel", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public boolean insanesurvivaloverhaul$alwaysRenderFoodWhileMounted(int left, int right, Operation<Boolean> original, @Local LivingEntity mount) {
        if (!Misc.fixMountsGui())
            return original.call(left, right);
        return true;
    }

    /**
     * Hides the jump meter when the mount's jump scale is 0 (not charging a jump),
     * preventing an empty jump bar from showing while riding.
     */
    @Definition(id = "playerrideablejumping", local = @Local(type = PlayerRideableJumping.class))
    @Expression("playerrideablejumping != null")
    @WrapOperation(method = "maybeRenderJumpMeter", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public boolean insanesurvivaloverhaul$hideEmptyJumpMeter(Object left, Object right, Operation<Boolean> original) {
        if (!Misc.fixMountsGui())
            return original.call(left, right);
        //noinspection DataFlowIssue
        return original.call(left, right) && (minecraft.player.getJumpRidingScale() > 0);
    }

    /**
     * Replaces the vanilla experience bar render check to also show XP while riding a mount
     * that has no active jump charge, so the XP bar is visible when the jump meter is hidden.
     */
    @Inject(method = "maybeRenderExperienceBar", at = @At(value = "HEAD"), cancellable = true)
    public void insanesurvivaloverhaul$showXpBarWhileMounted(GuiGraphics guiGraphics, DeltaTracker p_348543_, CallbackInfo ci) {
        if (!Misc.fixMountsGui())
            return;
        ci.cancel();
        int i = guiGraphics.guiWidth() / 2 - 91;
        if ((this.minecraft.player.jumpableVehicle() == null || minecraft.player.getJumpRidingScale() == 0) && this.minecraft.gameMode.hasExperience())
            this.renderExperienceBar(guiGraphics, i);
    }

    /**
     * Prevents the heart-shake animation from triggering at low health unless the player's
     * max health itself is low, avoiding distracting flicker with large health pools.
     */
    @Definition(id = "pCurrentHealth", local = @Local(type = int.class, ordinal = 4, argsOnly = true))
    @Definition(id = "pAbsorptionAmount", local = @Local(type = int.class, ordinal = 6, argsOnly = true))
    @Expression("pCurrentHealth + pAbsorptionAmount <= 4")
    @ModifyExpressionValue(method = "renderHearts", at = @At("MIXINEXTRAS:EXPRESSION"))
    public boolean insanesurvivaloverhaul$preventHealthShake(boolean original, @Local(argsOnly = true) float pMaxHealth) {
        if (!Misc.shouldPreventHealthShake() || !original)
            return original;
        return pMaxHealth > 4;
    }
}
