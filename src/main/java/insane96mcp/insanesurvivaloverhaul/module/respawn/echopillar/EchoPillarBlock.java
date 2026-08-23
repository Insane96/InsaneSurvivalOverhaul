package insane96mcp.insanesurvivaloverhaul.module.respawn.echopillar;

import com.google.common.collect.ImmutableList;
import insane96mcp.insanelib.data.SerializableMobEffectInstance;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public class EchoPillarBlock extends Block {
    public static final MutableComponent REQUIRES_CATALYST_MESSAGE = Component.translatable(InsaneSO.lang("requires_catalyst"));
    public static final MutableComponent PILLAR_DISABLED_MESSAGE = Component.translatable(InsaneSO.lang("pillar_disabled"));

    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final ImmutableList<Vec3i> RESPAWN_HORIZONTAL_OFFSETS = ImmutableList.of(
            new Vec3i(0, 0, 0),
            new Vec3i(0, 0, -1),
            new Vec3i(-1, 0, 0),
            new Vec3i(0, 0, 1),
            new Vec3i(1, 0, 0),
            new Vec3i(-1, 0, -1),
            new Vec3i(1, 0, -1),
            new Vec3i(-1, 0, 1),
            new Vec3i(1, 0, 1));
    private static final ImmutableList<Vec3i> RESPAWN_OFFSETS = (new ImmutableList.Builder<Vec3i>())
            .addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(offset -> offset.below(3)).iterator())
            .addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(offset -> offset.below(2)).iterator())
            .addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::below).iterator())
            .addAll(RESPAWN_HORIZONTAL_OFFSETS)
            .addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::above).iterator())
            .build();

    public EchoPillarBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.getStateDefinition().any().setValue(ENABLED, false).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER).setValue(ENABLED, false);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) != DoubleBlockHalf.UPPER)
            return super.canSurvive(state, level, pos);
        BlockState below = level.getBlockState(pos.below());
        return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos otherPos = half == DoubleBlockHalf.UPPER ? pos.below() : pos.above();
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(this) && otherState.getValue(HALF) != half) {
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 35);
                level.levelEvent(player, 2001, otherPos, Block.getId(otherState));
            }
            if (state.getValue(ENABLED) && level instanceof ServerLevel serverLevel) {
                BlockPos lowerPos = half == DoubleBlockHalf.UPPER ? pos.below() : pos;
                serverLevel.setChunkForced(lowerPos.getX() >> 4, lowerPos.getZ() >> 4, false);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
            state = level.getBlockState(pos);
            if (!state.is(this)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (state.getValue(ENABLED))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (!stack.is(EchoPillar.ECHO_PILLAR_CATALYST)) {
            if (player instanceof ServerPlayer serverPlayer && hand == InteractionHand.MAIN_HAND)
                serverPlayer.displayClientMessage(REQUIRES_CATALYST_MESSAGE, false);
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        enable(player, level, pos, state);
        stack.shrink(1);
        if (player instanceof ServerPlayer serverPlayer && (serverPlayer.getRespawnDimension() != level.dimension() || !pos.equals(serverPlayer.getRespawnPosition()))) {
            serverPlayer.setRespawnPosition(level.dimension(), pos, 0.0F, false, true);
            level.playSound(null, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
            EchoPillar.ACTIVATE_ECHO_PILLAR.get().trigger(serverPlayer);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
            state = level.getBlockState(pos);
            if (!state.is(this)) return InteractionResult.PASS;
        }

        if (state.getValue(ENABLED) && player.isCrouching()) {
            disable(player, level, pos, state, true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_55886_) {
        p_55886_.add(ENABLED, HALF);
    }

    public static void enable(@Nullable Entity entity, Level level, BlockPos pos, BlockState state) {
        state = state.setValue(ENABLED, true);
        level.setBlock(pos, state, 3);
        BlockState upperState = level.getBlockState(pos.above());
        if (upperState.is(state.getBlock()))
            level.setBlock(pos.above(), upperState.setValue(ENABLED, true), 3);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, state));
        level.playSound(null, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (level instanceof ServerLevel serverLevel)
            serverLevel.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
    }

    public static void disable(@Nullable Entity entity, Level level, BlockPos pos, BlockState state, boolean showDisabledMessage) {
        level.setBlock(pos, state.setValue(EchoPillarBlock.ENABLED, false), 3);
        BlockState upperState = level.getBlockState(pos.above());
        if (upperState.is(state.getBlock()))
            level.setBlock(pos.above(), upperState.setValue(ENABLED, false), 3);
        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
        if (entity instanceof ServerPlayer serverPlayer) {
            if (showDisabledMessage)
                serverPlayer.displayClientMessage(PILLAR_DISABLED_MESSAGE, false);
            serverPlayer.setRespawnPosition(Level.OVERWORLD, null, 0f, false, false);
        }
        if (level instanceof ServerLevel serverLevel)
            serverLevel.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, false);
        ItemEntity shard = new ItemEntity(level, pos.getCenter().x, pos.getY() + 1, pos.getCenter().z, new ItemStack(Items.ECHO_SHARD));
        level.addFreshEntity(shard);
    }

    @Override
    public Optional<ServerPlayer.RespawnPosAngle> getRespawnPosition(BlockState state, EntityType<?> type, LevelReader levelReader, BlockPos pos, float orientation) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
            state = levelReader.getBlockState(pos);
            if (!state.is(this)) return Optional.empty();
        }
        if (!state.getValue(EchoPillarBlock.ENABLED))
            return Optional.empty();
        if (state.getBlock() instanceof EchoPillarBlock)
            return findStandUpPosition(type, levelReader, pos);
        return Optional.empty();
    }

    public static Optional<ServerPlayer.RespawnPosAngle> findStandUpPosition(EntityType<?> pEntityType, CollisionGetter pLevel, BlockPos pPos) {
        Optional<ServerPlayer.RespawnPosAngle> optional = findStandUpPosition(pEntityType, pLevel, pPos, true);
        return optional.isPresent() ? optional : findStandUpPosition(pEntityType, pLevel, pPos, false);
    }

    private static Optional<ServerPlayer.RespawnPosAngle> findStandUpPosition(EntityType<?> pEntityType, CollisionGetter pLevel, BlockPos pPos, boolean pSimulate) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (Vec3i vec3i : RESPAWN_OFFSETS) {
            blockpos$mutableblockpos.set(pPos).move(vec3i);
            Vec3 vec3 = DismountHelper.findSafeDismountLocation(pEntityType, pLevel, blockpos$mutableblockpos, pSimulate);
            if (vec3 != null) {
                return Optional.of(ServerPlayer.RespawnPosAngle.of(vec3, pPos));
            }
        }

        return Optional.empty();
    }

    public static void onPillarRespawn(Player player, Level level, BlockPos respawnPos) {
        for (SerializableMobEffectInstance ISOMobEffectInstance : EchoPillar.echoPillarEffects) {
            player.addEffect(ISOMobEffectInstance.getMobEffectInstance());
        }
    }

    @Override
    public boolean isPossibleToRespawnInThis(BlockState pState) {
        return true;
    }

    public static int lightLevel(BlockState state) {
        return state.getValue(ENABLED) ? 15 : 7;
    }
}
