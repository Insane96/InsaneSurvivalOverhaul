package insane96mcp.insanesurvivaloverhaul.mixin.module.item.unvanishable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.items.UnvanishableItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class ItemMixin_UnvanishableItems {
	@ModifyReturnValue(method = "getBarWidth", at = @At("RETURN"))
	public int iguanatweaksreborn$onGetBarWidth(int original, ItemStack stack) {
		if (!Feature.isEnabled(UnvanishableItems.class)
				|| (!UnvanishableItems.isBroken(stack) && stack.getDamageValue() <= stack.getMaxDamage()))
			return original;

		return 13;
	}

	@ModifyReturnValue(method = "getBarColor", at = @At("RETURN"))
	public int iguanatweaksreborn$onGetBarColor(int original, ItemStack stack) {
		if (!Feature.isEnabled(UnvanishableItems.class)
				|| (!UnvanishableItems.isBroken(stack) && stack.getDamageValue() <= stack.getMaxDamage()))
			return original;

		return 16711680;
	}
}
