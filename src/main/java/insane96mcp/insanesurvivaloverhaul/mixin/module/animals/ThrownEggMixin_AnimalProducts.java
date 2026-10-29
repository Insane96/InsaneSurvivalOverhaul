package insane96mcp.insanesurvivaloverhaul.mixin.module.animals;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.animals.AnimalProducts;
import net.minecraft.world.entity.projectile.ThrownEgg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownEgg.class)
public class ThrownEggMixin_AnimalProducts {
    /**
     * Changes the chance (1 in this value) for a chicken to come out from a thrown egg
     */
    @ModifyExpressionValue(method = "onHit", at = @At(value = "CONSTANT", args = "intValue=8"))
    public int insanesurvivaloverhaul$chanceForChicken(int chance) {
        return !Feature.isEnabled(AnimalProducts.class) ? chance : AnimalProducts.chickenFromEggChance;
    }
}
