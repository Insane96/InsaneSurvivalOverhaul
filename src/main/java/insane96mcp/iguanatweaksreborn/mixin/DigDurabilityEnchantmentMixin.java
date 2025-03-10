package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DigDurabilityEnchantment.class)
public abstract class DigDurabilityEnchantmentMixin {
	@Inject(at = @At(value = "RETURN"), method = "shouldIgnoreDurabilityDrop", cancellable = true)
	private static void onChanceToIgnoreDurabilityDrop(ItemStack stack, int lvl, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
		if (EnchantmentsFeature.isUnbreakingOverhaul())
			cir.setReturnValue(false);
	}
}
