package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Shadow public abstract ListTag getEnchantmentTags();

	@ModifyReturnValue(method = "getMaxDamage", at = @At("RETURN"))
	public int iguanatwaeaksreborn$getMaxDamage(int original) {
		int maxDurability = original;
		int lvl = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.VANISHING_CURSE, (ItemStack) (Object) this);
		if (lvl > 0) {
			maxDurability += 500;
		}
		return maxDurability;
	}
}
