package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
	@Shadow public abstract ItemStack getItem();

	@Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
	public void onGetBarColor(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir) {
		if (!Feature.isEnabled(Tweaks.class)
				|| !this.getItem().is(Tweaks.WORLD_IMMUNE))
			return;

		if (!pSource.is(DamageTypes.FELL_OUT_OF_WORLD))
			cir.setReturnValue(false);
	}
}
