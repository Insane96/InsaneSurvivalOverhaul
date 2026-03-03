package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.potionandeffects;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.misc.PotionsAndEffects;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SplashPotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PotionItem.class)
public class PotionItemMixin_PotionsAndEffects {
    /**
     * Replaces the effect duration multiplier (1.0) shown in the splash potion tooltip
     * with {@link PotionsAndEffects#STREAMLINE_SPLASH_POTION_MULTIPLIER} when streamlined
     * splash potions are enabled, so the tooltip accurately reflects the reduced duration.
     */
    @ModifyExpressionValue(method = "appendHoverText", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
    public float insanesurvivaloverhaul$appendHoverText(float original) {
        return insanesurvivaloverhaul$self() instanceof SplashPotionItem && PotionsAndEffects.streamlineSplashPotions() ? PotionsAndEffects.STREAMLINE_SPLASH_POTION_MULTIPLIER : original;
    }

    @Unique
    public PotionItem insanesurvivaloverhaul$self() {
        return (PotionItem) (Object) this;
    }
}
