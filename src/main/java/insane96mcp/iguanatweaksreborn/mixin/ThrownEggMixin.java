package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.iguanatweaksreborn.module.farming.livestock.Livestock;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.entity.projectile.ThrownEgg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownEgg.class)
public class ThrownEggMixin {
    @ModifyExpressionValue(method = "onHit", at = @At(value = "CONSTANT", args = "intValue=8"))
    public int chanceForChicken(int chance) {
        return !Feature.isEnabled(Livestock.class) ? chance : Livestock.chickenFromEggChance;
    }
}
