package insane96mcp.iguanatweaksreborn.module.items.flintexpansion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class GroundRockBlock extends Block implements SimpleWaterloggedBlock {
    protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 2.0D, 11.0D);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public GroundRockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
        Vec3 vec3 = blockState.getOffset(blockGetter, pos);
        return SHAPE.move(vec3.x, vec3.y, vec3.z);
    }

    public boolean canSurvive(BlockState p_49395_, LevelReader p_49396_, BlockPos p_49397_) {
        return p_49396_.getBlockState(p_49397_.below()).isFaceSturdy(p_49396_, p_49397_.below(), Direction.DOWN.getOpposite());
    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState p_49031_, LevelAccessor levelAccessor, BlockPos pos, BlockPos p_49034_) {
        if (state.getValue(WATERLOGGED)) {
            levelAccessor.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return !state.canSurvive(levelAccessor, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, p_49031_, levelAccessor, pos, p_49034_);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }
}
