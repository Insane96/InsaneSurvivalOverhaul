package insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn;

import com.google.common.collect.ImmutableList;
import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.data.ITRMobEffectInstance;
import insane96mcp.iguanatweaksreborn.data.criterion.ITRTriggers;
import insane96mcp.iguanatweaksreborn.utils.ITRLogHelper;
import insane96mcp.insanelib.data.IdTagValue;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public class RespawnObeliskBlock extends Block {
    public static final String REQUIRES_CATALYST_LANG = IguanaTweaksReborn.MOD_ID + ".requires_catalyst";
    public static final String OBELISK_DISABLED = IguanaTweaksReborn.MOD_ID + ".obelisk_disabled";

    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    private static final ImmutableList<Vec3i> CATALYST_RELATIVE_POSITIONS = ImmutableList.of(
            /*new Vec3i(-2, 0, 0),
            new Vec3i(2, 0, 0),
            new Vec3i(0, 0, -2),
            new Vec3i(0, 0, 2),
            new Vec3i(-3, 0, 0),
            new Vec3i(3, 0, 0),
            new Vec3i(0, 0, -3),
            new Vec3i(0, 0, 3),*/
            new Vec3i(-4, 0, 0),
            new Vec3i(4, 0, 0),
            new Vec3i(0, 0, -4),
            new Vec3i(0, 0, 4)
    );

    private static final ImmutableList<Vec3i> RESPAWN_HORIZONTAL_OFFSETS = ImmutableList.of(new Vec3i(0, 0, -1), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(1, 0, 0), new Vec3i(-1, 0, -1), new Vec3i(1, 0, -1), new Vec3i(-1, 0, 1), new Vec3i(1, 0, 1));
    private static final ImmutableList<Vec3i> RESPAWN_OFFSETS = (new ImmutableList.Builder<Vec3i>()).addAll(RESPAWN_HORIZONTAL_OFFSETS).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::below).iterator()).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::above).iterator()).add(new Vec3i(0, 1, 0)).build();

    public RespawnObeliskBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.getStateDefinition().any().setValue(ENABLED, false));
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        if (!state.getValue(ENABLED)) {
            if (hasCatalysts(level, pos)) {
                enable(player, level, pos, state);
                return InteractionResult.SUCCESS;
            }
            else {
                if (!level.isClientSide && hand == InteractionHand.MAIN_HAND)
                    player.displayClientMessage(Component.translatable(REQUIRES_CATALYST_LANG), false);
                return InteractionResult.PASS;
            }
        }
        else if (state.getValue(ENABLED) && !level.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            if (!hasCatalysts(level, pos)) {
                disable(player, level, pos, state, true);
            }
            else if (serverPlayer.getRespawnDimension() != level.dimension() || !pos.equals(serverPlayer.getRespawnPosition())) {
                trySaveOldSpawn(serverPlayer);
                serverPlayer.setRespawnPosition(level.dimension(), pos, 0.0F, false, true);
                level.playSound(null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
                ITRTriggers.ACTIVATE_RESPAWN_OBELISK.trigger(serverPlayer);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_55886_) {
        p_55886_.add(ENABLED);
    }

    public static void enable(@Nullable Entity p_270997_, Level p_270172_, BlockPos p_270534_, BlockState p_270661_) {
        BlockState blockstate = p_270661_.setValue(ENABLED, true);
        p_270172_.setBlock(p_270534_, blockstate, 3);
        p_270172_.gameEvent(GameEvent.BLOCK_CHANGE, p_270534_, GameEvent.Context.of(p_270997_, blockstate));
        p_270172_.playSound(null, (double)p_270534_.getX() + 0.5D, (double)p_270534_.getY() + 0.5D, (double)p_270534_.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static void disable(@Nullable Entity entity, Level level, BlockPos pos, BlockState state, boolean showDisabledMessage) {
        level.setBlock(pos, state.setValue(RespawnObeliskBlock.ENABLED, false), 3);
        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        if (entity instanceof Player player && showDisabledMessage) {
            player.displayClientMessage(Component.translatable(OBELISK_DISABLED), false);
        }
        if (entity instanceof ServerPlayer serverPlayer) {
            if (!trySetOldSpawn(serverPlayer)) {
                serverPlayer.setRespawnPosition(Level.OVERWORLD, null, 0f, false, false);
            }
        }
    }

    @Override
    public Optional<Vec3> getRespawnPosition(BlockState state, EntityType<?> type, LevelReader levelReader, BlockPos pos, float orientation, @org.jetbrains.annotations.Nullable LivingEntity entity) {
        if (!levelReader.getBlockState(pos).getValue(RespawnObeliskBlock.ENABLED))
            return Optional.empty();
        if (state.getBlock() instanceof RespawnObeliskBlock)
            return RespawnAnchorBlock.findStandUpPosition(type, levelReader, pos);
        return Optional.empty();
    }

    public static void onObeliskRespawn(Player player, Level level, BlockPos respawnPos) {
        BlockPos.MutableBlockPos relativePos = new BlockPos.MutableBlockPos();
        for (Vec3i rel : CATALYST_RELATIVE_POSITIONS) {
            relativePos.set(respawnPos).move(rel);
            if (!isBlockCatalyst(level.getBlockState(relativePos).getBlock()))
                continue;
            double chance = getCatalystBlockChanceToBreak(level.getBlockState(relativePos).getBlock());
            if (chance > 0d && level.getRandom().nextDouble() < chance) {
                level.destroyBlock(relativePos, false);
            }
            //Try to break only one block
            break;
        }
        for (ITRMobEffectInstance itrMobEffectInstance : Respawn.respawnObeliskEffects) {
            player.addEffect(itrMobEffectInstance.getMobEffectInstance());
        }
        if (!hasCatalysts(level, respawnPos))
            disable(player, level, respawnPos, level.getBlockState(respawnPos), true);
    }

    public static boolean hasCatalysts(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos relativePos = new BlockPos.MutableBlockPos();
        for (Vec3i rel : CATALYST_RELATIVE_POSITIONS) {
            relativePos.set(pos).move(rel);
            if (isBlockCatalyst(level.getBlockState(relativePos).getBlock())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPossibleToRespawnInThis(BlockState pState) {
        return true;
    }

    public static void trySaveOldSpawn(ServerPlayer player) {
        if (player.getRespawnPosition() == null)
            return;

        saveOldSpawn(player, player.getRespawnPosition(), player.isRespawnForced(), player.getRespawnAngle(), player.getRespawnDimension());
    }

    public static boolean saveOldSpawn(ServerPlayer player, BlockPos pos, boolean isForced, float angle, ResourceKey<Level> dimension) {
        CompoundTag tag = MCUtils.getOrCreatePersistedData(player);
        if (tag.getInt("OldSpawnX") == pos.getX()
                && tag.getInt("OldSpawnY") == pos.getY()
                && tag.getInt("OldSpawnZ") == pos.getZ()
                && tag.getBoolean("OldSpawnForced") == isForced
                && tag.getFloat("OldSpawnAngle") == angle
                && Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, tag.get("OldSpawnDimension")).resultOrPartial(ITRLogHelper::error).orElse(Level.OVERWORLD) == dimension)
            return false;
        tag.putInt("OldSpawnX", pos.getX());
        tag.putInt("OldSpawnY", pos.getY());
        tag.putInt("OldSpawnZ", pos.getZ());
        tag.putBoolean("OldSpawnForced", isForced);
        tag.putFloat("OldSpawnAngle", angle);
        ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, dimension.location())
                .resultOrPartial(ITRLogHelper::error)
                .ifPresent((t) -> tag.put("OldSpawnDimension", t));
        return true;
    }

    public static boolean trySetOldSpawn(ServerPlayer player) {
        CompoundTag tag = MCUtils.getOrCreatePersistedData(player);
        if (!tag.contains("OldSpawnX"))
            return false;
        player.setRespawnPosition(Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, tag.get("OldSpawnDimension")).resultOrPartial(ITRLogHelper::error).orElse(Level.OVERWORLD), new BlockPos(tag.getInt("OldSpawnX"), tag.getInt("OldSpawnY"), tag.getInt("OldSpawnZ")), tag.getFloat("OldSpawnAngle"), tag.getBoolean("OldSpawnForced"), false);
        tag.remove("OldSpawnX");
        tag.remove("OldSpawnY");
        tag.remove("OldSpawnZ");
        tag.remove("OldSpawnAngle");
        tag.remove("OldSpawnForced");
        tag.remove("OldSpawnDimension");
        return true;
    }

    public static int lightLevel(BlockState state) {
        return state.getValue(ENABLED) ? 15 : 7;
    }

    public static boolean isBlockCatalyst(Block block) {
        return Respawn.respawnObeliskCatalysts.stream().anyMatch(idTagValue -> idTagValue.id.matchesBlock(block));
    }

    public static double getCatalystBlockChanceToBreak(Block block) {
        Optional<IdTagValue> catalyst = Respawn.respawnObeliskCatalysts.stream().filter(idTagValue -> idTagValue.id.matchesBlock(block)).findFirst();
        return catalyst.map(idTagValue -> idTagValue.value).orElse(0d);
    }
}
