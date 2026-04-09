package insane96mcp.insanesurvivaloverhaul.module.farming.crops;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WildCropBlock extends BushBlock {
    public static final MapCodec<WildCropBlock> CODEC = simpleCodec(WildCropBlock::new);
    public WildCropBlock(BlockBehaviour.Properties p_51021_) {
        super(p_51021_);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    protected boolean mayPlaceOn(BlockState p_51042_, BlockGetter p_51043_, BlockPos p_51044_) {
        return p_51042_.is(BlockTags.DIRT) || p_51042_.is(Blocks.FARMLAND) || p_51042_.is(BlockTags.SAND);
    }

}
