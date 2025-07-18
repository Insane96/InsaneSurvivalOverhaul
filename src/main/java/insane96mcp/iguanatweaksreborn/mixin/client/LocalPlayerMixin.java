package insane96mcp.iguanatweaksreborn.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import insane96mcp.iguanatweaksreborn.module.client.Death;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.healthregen.HealthRegenHunger;
import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    @Shadow public abstract boolean isUsingItem();

    public LocalPlayerMixin(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile);
    }

    //Stop sprinting when starting using an item
    @ModifyVariable(at = @At(value = "STORE"), method = "aiStep", index = 9)
    private boolean onAiStep(boolean flag8) {
        return flag8 || this.isUsingItem();
    }

    @Inject(method = "respawn", at = @At("TAIL"))
    public void onRequestRespawn(CallbackInfo ci) {
        if (Death.dead) {
            Death.dead = false;
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        }
    }

    @ModifyExpressionValue(method = "hasEnoughFoodToStartSprinting", at = @At(value = "CONSTANT", args = "floatValue=6.0"))
    public float insanesurvivaloverhaul$hasEnoughFoodToStartSprinting(float original) {
        if (!Feature.isEnabled(HealthRegenHunger.class))
            return original;

        return HealthRegenHunger.sprint$minHunger - 1;
    }

    @Definition(id = "hasEffect", method = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z")
    @Definition(id = "BLINDNESS", field = "Lnet/minecraft/world/effect/MobEffects;BLINDNESS:Lnet/minecraft/world/effect/MobEffect;")
    @Expression("this.hasEffect(BLINDNESS)")
    @ModifyExpressionValue(method = "canStartSprinting", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public boolean insanesurvivaloverhaul$blindnessPreventSprint(boolean original) {
        if (!Tweaks.doesBlindnessPreventSprint())
            return original;

        return false;
    }

    @Definition(id = "hasEffect", method = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z")
    @Definition(id = "BLINDNESS", field = "Lnet/minecraft/world/effect/MobEffects;BLINDNESS:Lnet/minecraft/world/effect/MobEffect;")
    @Expression("this.hasEffect(BLINDNESS)")
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public boolean insanesurvivaloverhaul$blindnessPreventSprint2(boolean original) {
        if (!Tweaks.doesBlindnessPreventSprint())
            return original;

        return false;
    }
}
