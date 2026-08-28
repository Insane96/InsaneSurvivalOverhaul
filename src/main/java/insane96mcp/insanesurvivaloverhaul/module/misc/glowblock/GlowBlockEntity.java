package insane96mcp.insanesurvivaloverhaul.module.misc.glowblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GlowBlockEntity extends BlockEntity {
    public GlowBlockEntity(BlockPos pos, BlockState state) {
        super(GlowBlockFeature.GLOW_BLOCK_ENTITY.get(), pos, state);
    }
}
