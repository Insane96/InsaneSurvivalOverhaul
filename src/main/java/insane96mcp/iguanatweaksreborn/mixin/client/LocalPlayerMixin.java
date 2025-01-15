package insane96mcp.iguanatweaksreborn.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import insane96mcp.iguanatweaksreborn.module.client.Death;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.healthregen.HealthRegen;
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
        if (!Feature.isEnabled(HealthRegen.class))
            return original;

        return HealthRegen.sprintMinHunger - 1;
    }
}
