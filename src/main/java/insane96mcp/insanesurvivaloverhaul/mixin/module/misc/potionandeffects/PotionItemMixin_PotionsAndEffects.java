package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.potionandeffects;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.misc.PotionsAndEffects;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SplashPotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PotionItem.class)
public class PotionItemMixin_PotionsAndEffects {
    @ModifyExpressionValue(method = "appendHoverText", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
    public float iguanatweaksreborn$appendHoverText(float original) {
        return self() instanceof SplashPotionItem && PotionsAndEffects.streamlineSplashPotions() ? PotionsAndEffects.STREAMLINE_SPLASH_POTION_MULTIPLIER : original;
    }

    public PotionItem self() {
        return (PotionItem) (Object) this;
    }
}
