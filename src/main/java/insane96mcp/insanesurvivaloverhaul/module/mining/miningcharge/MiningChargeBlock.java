package insane96mcp.insanesurvivaloverhaul.module.mining.miningcharge;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * A small TNT-like charge attached to the block it was placed on. {@link #FACING} points towards that block, which is
 * the direction of the tunnel dug by the {@link PrimedMiningCharge}.
 * Ignition (redstone, flint and steel, fire charges, burning projectiles, unstable state) is inherited from {@link TntBlock}.
 */
public class MiningChargeBlock extends TntBlock {
	public static final MapCodec<MiningChargeBlock> CODEC = simpleCodec(MiningChargeBlock::new);
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Map.of(
			Direction.DOWN, Block.box(2d, 0d, 2d, 14d, 6d, 14d),
			Direction.UP, Block.box(2d, 10d, 2d, 14d, 16d, 14d),
			Direction.NORTH, Block.box(2d, 2d, 0d, 14d, 14d, 6d),
			Direction.SOUTH, Block.box(2d, 2d, 10d, 14d, 14d, 16d),
			Direction.WEST, Block.box(0d, 2d, 2d, 6d, 14d, 14d),
			Direction.EAST, Block.box(10d, 2d, 2d, 16d, 14d, 14d)
	));

	public MiningChargeBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.DOWN));
	}

	@SuppressWarnings("unchecked")
	@Override
	public MapCodec<TntBlock> codec() {
		//TntBlock#codec is typed MapCodec<TntBlock>, so the subclass codec can't be returned without a cast
		return (MapCodec<TntBlock>) (MapCodec<?>) CODEC;
	}

	/**
	 * Spawns a primed Mining Charge facing the same direction as the block. The caller removes the block.
	 */
	private static PrimedMiningCharge createPrimed(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity igniter) {
		return new PrimedMiningCharge(level, pos, state.getValue(FACING), igniter);
	}

	@Override
	public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
		if (level.isClientSide)
			return;
		PrimedMiningCharge primed = createPrimed(level, pos, state, igniter);
		level.addFreshEntity(primed);
		level.playSound(null, primed.getX(), primed.getY(), primed.getZ(), MiningCharge.PRIMED_SOUND.get(), SoundSource.BLOCKS, 1f, 1.2f);
		level.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
	}

	/**
	 * Overridden instead of {@link #wasExploded} as the facing is needed and the block has already been removed when
	 * wasExploded is called. Like TNT, the charge is primed with a shorter, random fuse.
	 */
	@Override
	public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		if (level.isClientSide)
			return;
		PrimedMiningCharge primed = createPrimed(level, pos, state, explosion.getIndirectSourceEntity());
		int fuse = primed.getFuse();
		primed.setFuse((short) (level.random.nextInt(fuse / 4) + fuse / 8));
		level.addFreshEntity(primed);
	}

	/**
	 * Handled in {@link #onBlockExploded}, prevents TntBlock from spawning a vanilla Primed TNT.
	 */
	@Override
	public void wasExploded(Level level, BlockPos pos, Explosion explosion) {

	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 100;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 15;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
	}
}
