package insane96mcp.insanesurvivaloverhaul.module.items.explosivebarrel;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A TNT-like block that explodes instantly (no fuse) when powered, set on fire, hit by a burning projectile or caught in
 * another explosion. The explosion uses the TNT interaction, so destroyed blocks are all dropped (unless the
 * tntExplosionDropDecay gamerule is on).
 */
public class ExplosiveBarrelBlock extends DirectionalBlock {
	public static final MapCodec<ExplosiveBarrelBlock> CODEC = simpleCodec(ExplosiveBarrelBlock::new);

	public ExplosiveBarrelBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends DirectionalBlock> codec() {
		return CODEC;
	}

	/**
	 * Removes the barrel and makes it explode. The block is removed before exploding, otherwise the barrel would be
	 * caught in its own explosion and trigger a second one via {@link #wasExploded}.
	 */
	private static void explode(Level level, BlockPos pos, @Nullable Entity igniter) {
		if (level.isClientSide)
			return;
		level.removeBlock(pos, false);
		level.explode(igniter, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, ExplosiveBarrel.explosionPower.floatValue(), Level.ExplosionInteraction.TNT);
	}

	@Override
	public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction direction, @Nullable LivingEntity igniter) {
		explode(level, pos, igniter);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		if (!oldState.is(state.getBlock()) && level.hasNeighborSignal(pos))
			explode(level, pos, null);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
		if (level.hasNeighborSignal(pos))
			explode(level, pos, null);
	}

	@Override
	protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
		BlockPos pos = hit.getBlockPos();
		if (!level.isClientSide && projectile.isOnFire() && projectile.mayInteract(level, pos))
			explode(level, pos, projectile.getOwner());
	}

	@Override
	public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
		explode(level, pos, explosion.getIndirectSourceEntity());
	}

	@Override
	public boolean canDropFromExplosion(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
		return false;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
		tooltipComponents.add(Component.translatable(this.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.RED));
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 75;
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
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
	}
}
