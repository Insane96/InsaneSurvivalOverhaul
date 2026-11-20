package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth.healthregenhunger;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.HungerAndHealthRegen;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin_HealthRegenHunger {
    /**
     * Disables vanilla's peaceful natural regeneration (the NATURAL_REGENERATION game rule check) when
     * {@link HungerAndHealthRegen#peacefulHunger} is enabled, so that regeneration is handled by the feature.
     */
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    public boolean insanesurvivaloverhaul$onCheckPeacefulRegen(boolean original) {
        if (!Feature.isEnabled(HungerAndHealthRegen.class))
            return original;
        return original && !HungerAndHealthRegen.peacefulHunger;
    }
}
