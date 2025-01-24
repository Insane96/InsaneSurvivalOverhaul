package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.mining.MaterialsAndOres;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.OreVeinifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OreVeinifier.class)
public class OreVeinifierMixin {

	@Inject(method = "create", at = @At(value = "HEAD"), cancellable = true)
	private static void iguanatweaksreborn$onCreate(CallbackInfoReturnable<NoiseChunk.BlockStateFiller> cir) {
		if (Feature.isEnabled(MaterialsAndOres.class) && MaterialsAndOres.disableOreVeins)
			cir.setReturnValue(context -> null);
	}
}
