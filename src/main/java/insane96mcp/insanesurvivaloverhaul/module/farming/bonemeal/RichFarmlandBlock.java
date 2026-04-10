package insane96mcp.insanesurvivaloverhaul.module.farming.bonemeal;

import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.FarmlandWaterManager;

import javax.annotation.Nullable;

public class RichFarmlandBlock extends FarmBlock {
    public static final IntegerProperty RICHNESS = IntegerProperty.create("richness", 0, 7);;
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);
    public static final int MAX_RICHNESS = 7;
    public RichFarmlandBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, 0).setValue(RICHNESS, MAX_RICHNESS));
    }

    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            turnToDirt(null, state, level, pos);
        }
    }

    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);
        boolean isUnderCrops = isUnderCrops(level, pos);
        if (!isNearWater(level, pos) && !level.isRainingAt(pos.above())) {
            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
            }
            else {
                turnToFarmland(null, state, level, pos);
            }
        }
        else if (moisture < MAX_MOISTURE) {
            level.setBlock(pos, state.setValue(MOISTURE, MAX_MOISTURE), 2);
        }

        if (moisture == MAX_MOISTURE && isUnderCrops) {
            for (int e = 0; e < BoneMeal.richFarmland$extraTicks; e++) {
                level.getBlockState(pos.above()).randomTick(level, pos.above(), random);
            }
            //Bone meal particles
            level.levelEvent(2005, pos.above(), 0);
        }
        if (random.nextDouble() < BoneMeal.richFarmland$chanceToDecay) {
            loseRichness(null, state, level, pos);
        }
    }

    public static void turnToFarmland(@Nullable Entity entity, BlockState state, Level level, BlockPos pos) {
        BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(MOISTURE, state.getValue(MOISTURE));
        level.setBlockAndUpdate(pos, farmland);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, farmland));
    }

    public static void loseRichness(@Nullable Entity entity, BlockState state, Level level, BlockPos pos) {
        int richness = state.getValue(RICHNESS);
        if (richness == 0)
            turnToFarmland(entity, state, level, pos);
        else {
            BlockState newState = BoneMeal.RICH_FARMLAND.block().get().defaultBlockState().setValue(MOISTURE, state.getValue(MOISTURE)).setValue(RICHNESS, richness - 1);
            level.setBlockAndUpdate(pos, newState);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, newState));
        }
    }

    private static boolean isUnderCrops(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.above()).is(BlockTags.MAINTAINS_FARMLAND);
    }

    private static boolean isNearWater(LevelReader p_53259_, BlockPos p_53260_) {
        BlockState state = p_53259_.getBlockState(p_53260_);
        for(BlockPos blockpos : BlockPos.betweenClosed(p_53260_.offset(-Crops.getWaterHydrationRadius(), 0, -Crops.getWaterHydrationRadius()), p_53260_.offset(Crops.getWaterHydrationRadius(), 1, Crops.getWaterHydrationRadius()))) {
            if (state.canBeHydrated(p_53259_, p_53260_, p_53259_.getFluidState(blockpos), blockpos)) {
                return true;
            }
        }

        return FarmlandWaterManager.hasBlockWaterTicket(p_53259_, p_53260_);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(MOISTURE, RICHNESS);
    }
}
