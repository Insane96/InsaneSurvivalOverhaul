package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.item.ThrowablePotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ThrowablePotionItem.class, priority = 1001)
public class ThrowablePotionItemMixin {
	@ModifyExpressionValue(method = "use", at = @At(value = "CONSTANT", args = "floatValue=0.5F"))
	public float getUseDuration(float original) {
		if (!Feature.isEnabled(Tweaks.class))
			return original;

		return Tweaks.splashPotionThrowStrength.floatValue();
	}
}
