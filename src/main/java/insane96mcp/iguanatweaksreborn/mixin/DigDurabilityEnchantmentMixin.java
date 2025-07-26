package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DigDurabilityEnchantment.class)
public abstract class DigDurabilityEnchantmentMixin {
	@ModifyReturnValue(at = @At(value = "RETURN"), method = "shouldIgnoreDurabilityDrop")
	private static boolean iguanatweaksreborn$preventDurabilityDrop(boolean original) {
		if (EnchantmentsFeature.isUnbreakingOverhaulEnabled())
			return false;
		return original;
	}

	@ModifyReturnValue(at = @At(value = "RETURN"), method = "getMaxLevel")
	private int iguanatweaksreborn$maxLevel(int original) {
		if (EnchantmentsFeature.isUnbreakingOverhaulEnabled())
			return EnchantmentsFeature.unbreakingOverhaul$maxLevel;
		return original;
	}
}
