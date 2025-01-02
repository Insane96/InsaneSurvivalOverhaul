package insane96mcp.iguanatweaksreborn.module.world.coalfire;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoulSandBlock;
import net.minecraft.world.level.block.state.BlockState;

public class HellishCoalOreBlock extends SoulSandBlock {
    private final IntProvider xpRange;

    public HellishCoalOreBlock(Properties pProperties, IntProvider xpRange) {
        super(pProperties);
        this.xpRange = xpRange;
    }

    @Override
    public int getExpDrop(BlockState state, LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
        return silkTouchLevel == 0 ? this.xpRange.sample(randomSource) : 0;
    }
}
