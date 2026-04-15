package insane96mcp.insanesurvivaloverhaul.mixin.module.world.nether;

import insane96mcp.insanesurvivaloverhaul.module.world.Nether;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.SpringFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpringFeature.class)
public abstract class SpringFeatureMixin_Nether {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void insanesurvivaloverhaul$place(FeaturePlaceContext<SpringConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        if (Nether.shouldDisableLavaPockets(context.config()))
            cir.setReturnValue(false);
    }

}
