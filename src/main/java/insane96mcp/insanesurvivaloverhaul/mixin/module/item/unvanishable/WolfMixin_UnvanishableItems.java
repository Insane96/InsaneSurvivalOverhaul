package insane96mcp.insanesurvivaloverhaul.mixin.module.item.unvanishable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.items.unvanishableitems.UnvanishableItems;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Wolf.class)
public class WolfMixin_UnvanishableItems {
	/**
	 * Broken (unvanishable) wolf armor no longer absorbs damage, so the wolf takes it instead.
	 */
	@ModifyReturnValue(method = "canArmorAbsorb", at = @At("RETURN"))
	private boolean insanesurvivaloverhaul$onCanArmorAbsorb(boolean original) {
		if (!original
				|| !Feature.isEnabled(UnvanishableItems.class))
			return original;

		return !UnvanishableItems.isBroken(((Wolf) (Object) this).getBodyArmorItem());
	}
}
