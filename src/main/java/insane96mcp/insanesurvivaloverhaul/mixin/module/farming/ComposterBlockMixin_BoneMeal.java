package insane96mcp.insanesurvivaloverhaul.mixin.module.farming;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.farming.bonemeal.BoneMeal;
import net.minecraft.world.level.block.ComposterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ComposterBlock.class)
public abstract class ComposterBlockMixin_BoneMeal {
    @ModifyExpressionValue(method = "addItem", at = @At(value = "CONSTANT", args = "intValue=20"))
    private static int iguanatweaksreborn$composterTimeToProduce(int original) {
        return BoneMeal.composterTimeToProduce(original);
    }
    @ModifyExpressionValue(method = "onPlace", at = @At(value = "CONSTANT", args = "intValue=20"))
    private static int iguanatweaksreborn$composterTimeToProduceOnPlace(int original) {
        return BoneMeal.composterTimeToProduce(original);
    }
}
