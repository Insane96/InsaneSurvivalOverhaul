package insane96mcp.iguanatweaksreborn.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.client.Misc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow @Final protected Minecraft minecraft;

    @ModifyExpressionValue(method = "renderHotbar", at = @At(value = "CONSTANT", args = "intValue=22", ordinal = 0))
    public int hotBarShift(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }
    @ModifyExpressionValue(method = "renderHotbar", at = @At(value = "CONSTANT", args = "intValue=22", ordinal = 2))
    public int selectedHotBarShift(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }
    @ModifyExpressionValue(method = "renderHotbar", at = @At(value = "CONSTANT", args = "intValue=22", ordinal = 4))
    public int renderWholeSelectedHotBar(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return 24;
    }
    @ModifyExpressionValue(method = "renderHotbar", at = @At(value = "CONSTANT", args = "intValue=23", ordinal = 0))
    public int offHandHotbarShift(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }
    @ModifyExpressionValue(method = "renderHotbar", at = @At(value = "CONSTANT", args = "intValue=23", ordinal = 1))
    public int selectedOffHandHotbarShift(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }
    @ModifyExpressionValue(method = "renderHotbar", at = @At(value = "CONSTANT", args = "intValue=16", ordinal = 0))
    public int stacksShift(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }
    @ModifyExpressionValue(method = "renderHotbar", at = @At(value = "CONSTANT", args = "intValue=16", ordinal = 1))
    public int offHandStacksShift(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }
    @ModifyExpressionValue(method = "renderHotbar", at = @At(value = "CONSTANT", args = "intValue=20", ordinal = 2))
    public int attackIndicatorShift(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }
    @ModifyExpressionValue(method = "renderHotbar", at = @At(value = "CONSTANT", args = "intValue=18", ordinal = 2))
    public int attackIndicatorShift2(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }
    @ModifyExpressionValue(method = "renderExperienceBar", at = @At(value = "CONSTANT", args = "intValue=32", ordinal = 0))
    public int experienceBarShift(int original) {
        if (!Misc.shouldRaiseHotbar())
            return original;
        return original + Misc.floatyHotbar;
    }
    @ModifyExpressionValue(method = "renderExperienceBar", at = @At(value = "CONSTANT", args = "intValue=31", ordinal = 0))
    public int experienceBarLevelsShift(int original) {
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
    public int iguanatweaksreborn$onGetVehicleHealth(int original, @Local LivingEntity mount) {
        if (!Misc.fixMountsGui())
            return original;
        return (int)(mount.getMaxHealth() + 0.5F) / 2;
    }

    @Definition(id = "pCurrentHealth", local = @Local(type = int.class, ordinal = 4, argsOnly = true))
    @Definition(id = "pAbsorptionAmount", local = @Local(type = int.class, ordinal = 6, argsOnly = true))
    @Expression("pCurrentHealth + pAbsorptionAmount <= 4")
    @ModifyExpressionValue(method = "renderHearts", at = @At("MIXINEXTRAS:EXPRESSION"))
    public boolean iguanatweaksreborn$onShakeCheck(boolean original, @Local(argsOnly = true) float pMaxHealth) {
        if (!Misc.shouldPreventHealthShake()
                || !original)
            return original;
        return pMaxHealth > 4;
    }
}
