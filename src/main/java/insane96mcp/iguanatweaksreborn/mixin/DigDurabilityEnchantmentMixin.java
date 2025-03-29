package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DigDurabilityEnchantment.class)
public abstract class DigDurabilityEnchantmentMixin {
	@ModifyReturnValue(at = @At(value = "RETURN"), method = "shouldIgnoreDurabilityDrop")
	private static boolean onChanceToIgnoreDurabilityDrop(boolean original) {
		if (EnchantmentsFeature.isUnbreakingOverhaul())
			return false;
		return original;
	}

	@ModifyReturnValue(at = @At(value = "RETURN"), method = "getMaxLevel")
	private int onChanceToIgnoreDurabilityDrop(int original) {
		if (EnchantmentsFeature.isUnbreakingOverhaul())
			return 5;
		return original;
	}
}
