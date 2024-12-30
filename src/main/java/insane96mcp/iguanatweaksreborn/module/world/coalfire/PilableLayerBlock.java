package insane96mcp.iguanatweaksreborn.module.world.coalfire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class PilableLayerBlock extends SnowLayerBlock implements Fallable {
    public static final int MAX_HEIGHT = 8;
    @Nullable
    private final Item itemPlacer;
    public PilableLayerBlock(Properties properties) {
        this(properties, null);
    }

    public PilableLayerBlock(Properties properties, Item itemPlacer) {
        super(properties);
        this.itemPlacer = itemPlacer;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.is(this)) {
            int i = blockstate.getValue(LAYERS);
            return blockstate.setValue(LAYERS, Math.min(8, i + 1));
        } else {
            return super.getStateForPlacement(context);
        }
    }

    public static boolean isFree(BlockState state) {
        return state.isAir() || state.is(BlockTags.FIRE) || state.liquid() || (state.canBeReplaced() && !(state.getBlock() instanceof PilableLayerBlock));
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(16) == 0) {
            BlockPos blockpos = pos.below();
            if (isFree(level.getBlockState(blockpos))) {
                ParticleUtils.spawnParticleBelow(level, pos, random, new BlockParticleOption(ParticleTypes.FALLING_DUST, state));
            }
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_56625_, BlockGetter p_56626_, BlockPos p_56627_, CollisionContext p_56628_) {
        return SHAPE_BY_LAYER[p_56625_.getValue(LAYERS)];
    }

    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState state1, boolean p_53237_) {
        level.scheduleTick(pos, this, this.getDelayAfterPlace());
    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState state1, LevelAccessor levelAccessor, BlockPos pos, BlockPos pos1) {
        levelAccessor.scheduleTick(pos, this, this.getDelayAfterPlace());
        return super.updateShape(state, direction, state1, levelAccessor, pos, pos1);
    }

    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState stateBelow = level.getBlockState(pos.below());
        //Don't fall if there are 8 layers below (is full block)
        if (isFree(stateBelow) || (stateBelow.is(this) && stateBelow.getValue(LAYERS) < MAX_HEIGHT)) {
            if (pos.getY() >= level.getMinBuildHeight()) {
                PilableFallingLayerEntity fallingblockentity = PilableFallingLayerEntity.fall(level, pos, state);
                this.falling(fallingblockentity);
            }
        }
    }

    protected void falling(FallingBlockEntity p_53206_) {
    }

    protected int getDelayAfterPlace() {
        return 2;
    }

    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        int i = state.getValue(LAYERS);
        return (context.getItemInHand().is(this.asItem()) || (this.itemPlacer != null && context.getItemInHand().is(this.itemPlacer))) && i < MAX_HEIGHT;
    }

    public boolean canSurvive(BlockState state, LevelReader levelReader, BlockPos pos) {
        return true;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @org.jetbrains.annotations.Nullable BlockEntity blockEntity, ItemStack stack) {
        /*player.awardStat(MiscStats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        if (stack.getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0 && this.asItem() != Items.AIR) {
            popResource(level, pos, new ItemStack(this.asItem()));
        }
        else {
            popResource(level, pos, new ItemStack(this.itemDropped));
        }*/
        super.playerDestroy(level, player, pos, state, blockEntity, stack);
        if (state.getValue(LAYERS) > 1)
            level.setBlock(pos, state.setValue(LAYERS, state.getValue(LAYERS) - 1), 3);
    }
}
