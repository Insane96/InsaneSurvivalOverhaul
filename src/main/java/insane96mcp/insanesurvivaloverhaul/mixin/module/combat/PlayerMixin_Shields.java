package insane96mcp.insanesurvivaloverhaul.mixin.module.combat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.combat.Shields;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin_Shields {

    /**
     * Overrides the minimum damage required to damage a shield (vanilla: 3.0) with the configured value.
     * @see Shields#getMinHurtDamage(float)
     */
    @ModifyExpressionValue(method = "hurtCurrentlyUsedShield", at = @At(value = "CONSTANT", args = "floatValue=3.0"))
    private float insanesurvivaloverhaul$shieldMinHurtDamage(float original) {
        return Shields.getMinHurtDamage(original);
    }
}
