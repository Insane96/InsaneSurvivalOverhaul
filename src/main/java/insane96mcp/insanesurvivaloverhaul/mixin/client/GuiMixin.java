package insane96mcp.insanesurvivaloverhaul.mixin.client;

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
public abstract class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract boolean isExperienceBarVisible();

    @Shadow
    protected abstract void renderExperienceBar(GuiGraphics guiGraphics, int x);

    @ModifyExpressionValue(method = "renderItemHotbar", at = @At(value = "CONSTANT", args = "intValue=22", ordinal = 0))
    public int hotBarShift(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }

    @ModifyExpressionValue(method = "renderItemHotbar", at = @At(value = "CONSTANT", args = "intValue=22", ordinal = 2))
    public int selectedHotBarShift(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }

    @ModifyExpressionValue(method = "renderItemHotbar", at = @At(value = "CONSTANT", args = "intValue=23", ordinal = 1))
    public int offHandShiftLeft(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }

    @ModifyExpressionValue(method = "renderItemHotbar", at = @At(value = "CONSTANT", args = "intValue=23", ordinal = 2))
    public int offHandShiftRight(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }

    @ModifyExpressionValue(method = "renderItemHotbar", at = @At(value = "CONSTANT", args = "intValue=16", ordinal = 0))
    public int renderItems(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }

    @ModifyExpressionValue(method = "renderItemHotbar", at = @At(value = "CONSTANT", args = "intValue=16", ordinal = 1))
    public int renderItemsOffhand(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }

    @ModifyExpressionValue(method = "renderItemHotbar", at = @At(value = "CONSTANT", args = "intValue=20", ordinal = 2))
    public int renderAttackIndicator(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }

    @ModifyExpressionValue(method = "renderExperienceBar", at = @At(value = "CONSTANT", args = "intValue=32", ordinal = 0))
    public int experienceBarAndLevelShift(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }

    @ModifyExpressionValue(method = "renderJumpMeter", at = @At(value = "CONSTANT", args = "intValue=32", ordinal = 0))
    public int jumpMeterShift(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }

    @ModifyVariable(method = "renderVehicleHealth", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    public int insanesurvivaloverhaul$onGetVehicleHealth(int original, @Local LivingEntity mount) {
        if (!Misc.fixMountsGui())
            return original;
        return (int)(mount.getMaxHealth() + 0.5F) / 2;
    }

    @Definition(id = "l2", local = @Local(type = int.class))
    @Expression("l2 == 0")
    @WrapOperation(method = "renderFoodLevel", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public boolean insanesurvivaloverhaul$onTryRenderFood(int left, int right, Operation<Boolean> original, @Local LivingEntity mount) {
        if (!Misc.fixMountsGui())
            return original.call(left, right);
        return true;
    }

    @Definition(id = "playerrideablejumping", local = @Local(type = PlayerRideableJumping.class))
    @Expression("playerrideablejumping != null")
    @WrapOperation(method = "maybeRenderJumpMeter", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public boolean insanesurvivaloverhaul$onGetVehicleHealth(Object left, Object right, Operation<Boolean> original) {
        if (!Misc.fixMountsGui())
            return original.call(left, right);
        //noinspection DataFlowIssue
        return original.call(left, right) && (minecraft.player.getJumpRidingScale() > 0/* || DroppedExperience.disableExperience*/);
    }

    @Inject(method = "maybeRenderExperienceBar", at = @At(value = "HEAD"), cancellable = true)
    public void insanesurvivaloverhaul$onTryRenderXpBar(GuiGraphics guiGraphics, DeltaTracker p_348543_, CallbackInfo ci) {
        if (!Misc.fixMountsGui())
            return;
        ci.cancel();
        int i = guiGraphics.guiWidth() / 2 - 91;
        if ((this.minecraft.player.jumpableVehicle() == null || minecraft.player.getJumpRidingScale() == 0) && this.minecraft.gameMode.hasExperience()) {
            this.renderExperienceBar(guiGraphics, i);
        }
    }

    @Definition(id = "pCurrentHealth", local = @Local(type = int.class, ordinal = 4, argsOnly = true))
    @Definition(id = "pAbsorptionAmount", local = @Local(type = int.class, ordinal = 6, argsOnly = true))
    @Expression("pCurrentHealth + pAbsorptionAmount <= 4")
    @ModifyExpressionValue(method = "renderHearts", at = @At("MIXINEXTRAS:EXPRESSION"))
    public boolean insanesurvivaloverhaul$onShakeCheck(boolean original, @Local(argsOnly = true) float pMaxHealth) {
        if (!Misc.shouldPreventHealthShake()
                || !original)
            return original;
        return pMaxHealth > 4;
    }
}
