package insane96mcp.insanesurvivaloverhaul.module.mobs.spawning;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class EchoLanternBlock extends LanternBlock {
    public EchoLanternBlock(Properties pProperties) {
        super(pProperties);
    }

    /**
     * Tracks the lantern however it's placed (player, /setblock, structures, etc.)
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel && !oldState.is(this))
            EchoLanternSavedData.get(serverLevel).addPosition(pos);
    }

    /**
     * Stops tracking the lantern however it's removed (player, explosions, pistons, support removed, /setblock, etc.)
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel && !newState.is(this))
            EchoLanternSavedData.get(serverLevel).removePosition(pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
