package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.potionandeffects;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.misc.PotionsAndEffects;
import net.minecraft.world.item.ThrowablePotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ThrowablePotionItem.class, priority = 1001)
public class ThrowablePotionItemMixin_PotionsAndEffects {
	@ModifyExpressionValue(method = "use", at = @At(value = "CONSTANT", args = "floatValue=0.5F"))
	public float getUseDuration(float original) {
		if (!Feature.isEnabled(PotionsAndEffects.class))
			return original;

		return PotionsAndEffects.splashPotionThrowStrength.floatValue();
	}
}
