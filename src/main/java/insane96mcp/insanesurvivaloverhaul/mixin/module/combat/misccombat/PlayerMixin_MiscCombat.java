package insane96mcp.insanesurvivaloverhaul.mixin.module.combat.misccombat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.combat.MiscCombat;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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

}
