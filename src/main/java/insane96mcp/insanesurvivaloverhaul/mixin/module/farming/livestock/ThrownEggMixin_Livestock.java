package insane96mcp.insanesurvivaloverhaul.mixin.module.farming.livestock;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.farming.livestock.Livestock;
import net.minecraft.world.entity.projectile.ThrownEgg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownEgg.class)
public class ThrownEggMixin_Livestock {
    @ModifyExpressionValue(method = "onHit", at = @At(value = "CONSTANT", args = "intValue=8"))
    public int chanceForChicken(int chance) {
        return !Feature.isEnabled(Livestock.class) ? chance : Livestock.chickenFromEggChance;
    }
}
