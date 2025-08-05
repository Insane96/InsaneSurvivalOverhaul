package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.iguanatweaksreborn.module.world.seasons.Seasons;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpreadingSnowyDirtBlock.class)
public class SpreadingSnowyDirtBlockMixin {

    @ModifyReturnValue(method = "canPropagate", at = @At(value = "RETURN"))
    private static boolean iguanatweaksreborn$slowdownSpreadingGrass(boolean original, BlockState pState, LevelReader pLevel, BlockPos pPos) {
        if (!Seasons.shouldSlowdownGrassSpreading(pLevel))
            return original;
        return false;
    }
}
