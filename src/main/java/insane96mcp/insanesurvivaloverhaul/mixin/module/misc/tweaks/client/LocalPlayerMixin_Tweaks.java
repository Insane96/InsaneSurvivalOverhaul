package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin_Tweaks {
    /**
     * Prevents the local player from starting to sprint while under the Blindness effect,
     * by forcing the blindness check in {@code canStartSprinting} to return {@code false}.
     */
    @Definition(id = "hasEffect", method = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z")
    @Definition(id = "BLINDNESS", field = "Lnet/minecraft/world/effect/MobEffects;BLINDNESS:Lnet/minecraft/core/Holder;")
    @Expression("this.hasEffect(BLINDNESS)")
    @ModifyExpressionValue(method = "canStartSprinting", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public boolean insanesurvivaloverhaul$blindnessPreventSprint(boolean original) {
        if (!Tweaks.doesBlindnessPreventSprint())
            return original;

        return false;
    }

    /**
     * Prevents the local player from continuing to sprint during {@code aiStep} while under
     * the Blindness effect, complementing the check in {@code canStartSprinting}.
     */
    @Definition(id = "hasEffect", method = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z")
    @Definition(id = "BLINDNESS", field = "Lnet/minecraft/world/effect/MobEffects;BLINDNESS:Lnet/minecraft/core/Holder;")
    @Expression("this.hasEffect(BLINDNESS)")
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public boolean insanesurvivaloverhaul$blindnessPreventSprintInAiStep(boolean original) {
        if (!Tweaks.doesBlindnessPreventSprint())
            return original;

        return false;
    }
}
