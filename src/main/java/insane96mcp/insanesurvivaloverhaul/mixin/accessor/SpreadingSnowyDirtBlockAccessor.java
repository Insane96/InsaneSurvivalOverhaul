package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpreadingSnowyDirtBlock.class)
public interface SpreadingSnowyDirtBlockAccessor {
    @Invoker("canBeGrass")
    static boolean callCanBeGrass(BlockState state, LevelReader levelReader, BlockPos pos) {
        throw new AssertionError();
    }
}
