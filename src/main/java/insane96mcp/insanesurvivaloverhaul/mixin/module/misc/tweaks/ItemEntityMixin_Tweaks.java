package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
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
public abstract class ItemEntityMixin_Tweaks {
	@Shadow
	public abstract ItemStack getItem();

	/**
	 * Cancels damage dealt to item entities whose item is tagged as world-immune, except
	 * for falling out of the world, so they survive fire, lava, explosions, etc.
	 */
	@Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
	public void insanesurvivaloverhaul$onHurt(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir) {
		if (!Feature.isEnabled(Tweaks.class)
				|| !this.getItem().is(Tweaks.WORLD_IMMUNE))
			return;

		if (!pSource.is(DamageTypes.FELL_OUT_OF_WORLD))
			cir.setReturnValue(false);
	}
}
