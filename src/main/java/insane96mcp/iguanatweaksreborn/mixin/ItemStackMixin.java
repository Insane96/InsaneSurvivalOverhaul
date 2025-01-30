package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.iguanatweaksreborn.event.ISOEventFactory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@ModifyReturnValue(method = "getMaxDamage", at = @At("RETURN"))
	public int iguanatweaksreborn$getMaxDamage(int original) {
		return ISOEventFactory.getStackMaxDamage((ItemStack) (Object) this, original);
	}
}
