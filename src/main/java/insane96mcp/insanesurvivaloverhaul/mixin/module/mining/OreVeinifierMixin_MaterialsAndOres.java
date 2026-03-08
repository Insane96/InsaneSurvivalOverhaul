package insane96mcp.insanesurvivaloverhaul.mixin.module.mining;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.mining.MaterialsAndOres;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.OreVeinifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OreVeinifier.class)
public class OreVeinifierMixin_MaterialsAndOres {

	@Inject(method = "create", at = @At(value = "HEAD"), cancellable = true)
	private static void iguanatweaksreborn$onCreate(CallbackInfoReturnable<NoiseChunk.BlockStateFiller> cir) {
		if (Feature.isEnabled(MaterialsAndOres.class) && MaterialsAndOres.disableOreVeins)
			cir.setReturnValue(context -> null);
	}
}
