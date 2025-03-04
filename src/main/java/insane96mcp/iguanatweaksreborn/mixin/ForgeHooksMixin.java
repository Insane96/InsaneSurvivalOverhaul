package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ForgeHooks.class)
public abstract class ForgeHooksMixin {
	@ModifyExpressionValue(method = "dropXpForBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;)I", ordinal = 0), remap = false)
    private static int iguanatweaksreborn$onItemEnchantmentLevel(int original) {
		return EnchantmentsFeature.getLuckLevel(original, original);
	}
}
