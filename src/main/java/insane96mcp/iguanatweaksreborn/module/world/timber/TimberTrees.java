package insane96mcp.iguanatweaksreborn.module.world.timber;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.data.generator.ISOBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.mixin.FallingBlockEntityAccessor;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@LoadFeature(module = Modules.Ids.WORLD, description = "Trees fall when cut. This feature uses InsaneLib's Better Falling Blocks feature. Disabling that might result in blocks dropping when falling.")
public class TimberTrees extends JsonFeature {

    public static final TagKey<Block> TIMBER_TRUNKS = ISOBlockTagsProvider.create("timber_trunks");
    public static final TagKey<Block> ATTACHED_BLOCKS = ISOBlockTagsProvider.create("attached_blocks");
    public static final TagKey<Item> BLACKLISTED_ITEMS = ISOItemTagsProvider.create("no_timber");

    public static final ArrayList<TreeInfo> TREE_INFOS_DEFAULT = new ArrayList<>(List.of(
            new TreeInfo.Builder().log(IdTagMatcher.newId("minecraft:oak_log")).leaves(IdTagMatcher.newTag("iguanatweaksreborn:oak_log_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("minecraft:spruce_log")).leaves(IdTagMatcher.newId("minecraft:spruce_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("minecraft:birch_log")).leaves(IdTagMatcher.newId("minecraft:birch_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("minecraft:jungle_log")).leaves(IdTagMatcher.newId("minecraft:jungle_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("minecraft:dark_oak_log")).leaves(IdTagMatcher.newId("minecraft:dark_oak_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("minecraft:acacia_log")).leaves(IdTagMatcher.newId("minecraft:acacia_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("minecraft:cherry_log")).leaves(IdTagMatcher.newId("minecraft:cherry_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("minecraft:mangrove_log")).leaves(IdTagMatcher.newId("minecraft:mangrove_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("quark:blossom_log")).leaves(IdTagMatcher.newTag("iguanatweaksreborn:trumpet_leaves")).logsSidewaysRatio(0.3f).maxDistanceFromLogs(15).decayPercentage(0.35f).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("quark:azalea_log")).leaves(IdTagMatcher.newTag("iguanatweaksreborn:azalea_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("quark:ancient_log")).leaves(IdTagMatcher.newId("quark:ancient_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("autumnity:maple_log")).leaves(IdTagMatcher.newTag("iguanatweaksreborn:maple_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("upgrade_aquatic:river_log")).leaves(IdTagMatcher.newId("upgrade_aquatic:river_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("atmospheric:rosewood_log")).leaves(IdTagMatcher.newId("atmospheric:rosewood_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("atmospheric:yucca_log")).leaves(IdTagMatcher.newId("atmospheric:yucca_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("atmospheric:grimwood_log")).leaves(IdTagMatcher.newId("atmospheric:grimwood_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("atmospheric:kousa_log")).leaves(IdTagMatcher.newId("atmospheric:kousa_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newTag("iguanatweaksreborn:aspen_logs")).leaves(IdTagMatcher.newTag("iguanatweaksreborn:aspen_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("atmospheric:laurel_log")).leaves(IdTagMatcher.newTag("iguanatweaksreborn:laurel_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("atmospheric:morado_log")).leaves(IdTagMatcher.newTag("iguanatweaksreborn:morado_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("environmental:pine_log")).leaves(IdTagMatcher.newId("environmental:pine_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("environmental:plum_log")).leaves(IdTagMatcher.newTag("iguanatweaksreborn:plum_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("environmental:willow_log")).leaves(IdTagMatcher.newId("environmental:willow_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("environmental:wisteria_log")).leaves(IdTagMatcher.newTag("iguanatweaksreborn:wisteria_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("caverns_and_chasms:azalea_log")).leaves(IdTagMatcher.newTag("iguanatweaksreborn:azalea_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("tconstruct:greenheart_log")).leaves(IdTagMatcher.newId("tconstruct:earth_slime_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("tconstruct:skyroot_log")).leaves(IdTagMatcher.newId("tconstruct:sky_slime_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("tconstruct:enderbark_log")).leaves(IdTagMatcher.newId("tconstruct:ender_slime_leaves")).build()
    ));
    public static final ArrayList<TreeInfo> treeInfos = new ArrayList<>();

    @Config(name = "Requires #minecraft:axes", description = "Trees will timber only if an item in the minecraft:axes item tag is used")
    public static Boolean requiresAxe = false;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        JSON_CONFIGS.add(new JsonConfig<>("tree_info.json", treeInfos, TREE_INFOS_DEFAULT, TreeInfo.LIST_TYPE));
    }

    @Override
    public String getModConfigFolder() {
        return InsaneSO.CONFIG_FOLDER;
    }

    @SubscribeEvent
    public void onLogBreak(BlockEvent.BreakEvent event) {
        if (!this.isEnabled()
            || !event.getState().is(TIMBER_TRUNKS)
            || !(event.getState().getBlock() instanceof RotatedPillarBlock)
            || event.getState().getValue(RotatedPillarBlock.AXIS) != Direction.Axis.Y
            || (requiresAxe && !(event.getPlayer().getMainHandItem().is(ItemTags.AXES)))
            || event.getPlayer().getMainHandItem().is(BLACKLISTED_ITEMS))
            return;

        BlockPos brokenPos = event.getPos();
        Level level = (Level) event.getLevel();
        if (level.getBlockState(brokenPos.north()).is(event.getState().getBlock())
                || level.getBlockState(brokenPos.south()).is(event.getState().getBlock())
                || level.getBlockState(brokenPos.east()).is(event.getState().getBlock())
                || level.getBlockState(brokenPos.west()).is(event.getState().getBlock()))
            return;
        TreeInfo treeInfo = treeInfos.stream().filter(ti -> ti.log.matchesBlock(event.getState())).findFirst().orElse(new TreeInfo());
        List<BlockPos> blocks = getTreeBlocks(brokenPos, event.getState(), level, treeInfo);
        Direction direction = event.getPlayer().getDirection();
        boolean hasBrokenLeaves = false;
        for (BlockPos pos : blocks) {
            if (pos.equals(brokenPos))
                continue;
            Vec3i relative = new BlockPos(pos.getX() - brokenPos.getX(), pos.getY() - brokenPos.getY(), pos.getZ() - brokenPos.getZ());
            int verticalDistance = relative.getY();
            int horizontalDistance;
            if (direction.getAxis() == Direction.Axis.X)
                horizontalDistance = relative.getX();
            else
                horizontalDistance = relative.getZ();
            horizontalDistance *= direction.getAxisDirection().opposite().getStep();
            BlockState state = level.getBlockState(pos);
            // Logs fall lower (closer to ground), leaves fall higher
            int verticalOffset = state.is(TIMBER_TRUNKS) ? 0 : horizontalDistance;
            BlockPos fallingBlockPos = pos.relative(direction, verticalDistance + horizontalDistance).above(verticalOffset);
            if (state.is(BlockTags.LEAVES) && level.getRandom().nextFloat() < treeInfo.decayPercentage) {
                BlockEntity blockentity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
                Block.dropResources(state, level, pos, blockentity, event.getPlayer(), ItemStack.EMPTY);
                level.removeBlock(pos, false);
                if (!hasBrokenLeaves) {
                    level.playSound(null, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1f, 1.0f);
                    hasBrokenLeaves = true;
                }
                continue;
            }
            if (state.getBlock() instanceof RotatedPillarBlock)
                state = rotatePillar(state, direction.getAxis());
            //TODO Re-integrate direction?
            FallingBlockEntity fallingBlock = FallingBlockEntityAccessor.ctor(level, Vec3.atCenterOf(fallingBlockPos).x, Vec3.atCenterOf(fallingBlockPos).y + horizontalDistance, Vec3.atCenterOf(fallingBlockPos).z, state);
            if (state.hasBlockEntity()) {
                //noinspection DataFlowIssue
                fallingBlock.blockData = level.getBlockEntity(pos).saveWithoutMetadata();
                if (level.getBlockEntity(pos) instanceof BeehiveBlockEntity beehiveBlockEntity)
                    beehiveBlockEntity.emptyAllLivingFromHive(event.getPlayer(), state, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
            }
            if (state.is(TIMBER_TRUNKS))
                fallingBlock.setHurtsEntities(0.5f, 20);
            else
                fallingBlock.setHurtsEntities(0.1f, 4);
            level.addFreshEntity(fallingBlock);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static List<BlockPos> getTreeBlocks(BlockPos pos, BlockState state, Level level, TreeInfo treeInfo) {
        Set<BlockPos> visitedBlocks = new HashSet<>();
        Queue<BlockPos> posToCheck = new ArrayDeque<>();

        int checks = 0;
        int logs = 0;
        int sidewaysLogs = 0;
        int maxY = pos.getY();
        boolean foundLeaves = false;
        int debugCounter = 0;

        IdTagMatcher validLogs = Objects.requireNonNullElseGet(treeInfo.log,
            () -> IdTagMatcher.newId(ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString()));
        IdTagMatcher validLeaves = treeInfo.leaves;

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        BlockPos startPos = pos.immutable();

        // Initialize: check if there's a log above or around
        if (!initializeTreeSearch(pos, level, validLogs, visitedBlocks, posToCheck)) {
            return new ArrayList<>();
        }

        logs++;
        visitedBlocks.add(startPos);
        posToCheck.add(startPos);

        // Main search loop
        while (checks < 10000 && !posToCheck.isEmpty()) {
            BlockPos currentPos = posToCheck.poll();
            BlockState currentState = level.getBlockState(currentPos);

            for (BlockPos neighbor : getPositionsToCheck(currentPos, currentState)) {
                if (++checks >= 10000) break;

                mutablePos.set(neighbor);
                BlockState neighborState = level.getBlockState(mutablePos);

                if (neighborState.isAir()) continue;

                BlockPos neighborImmutable = mutablePos.immutable();
                if (visitedBlocks.contains(neighborImmutable)) continue;

                // Check if this block should be added to the tree
                BlockCheckResult result = checkBlock(neighborImmutable, neighborState, currentState,
                    pos, validLogs, validLeaves, treeInfo);

                if (result.shouldAdd) {
                    visitedBlocks.add(neighborImmutable);

                    if (!result.isAttached) {
                        posToCheck.add(neighborImmutable);
                    }

                    if (!FMLLoader.isProduction()) {
                        Display.TextDisplay display = EntityType.TEXT_DISPLAY.create(level);
                        if (display != null) {
                            display.setPos(neighborImmutable.getCenter());
                            int distance = neighborState.is(BlockTags.LEAVES)
                                ? neighborState.getValue(LeavesBlock.DISTANCE)
                                : -1;
                            display.setText(Component.literal(debugCounter++ + ": " + distance));
                            level.addFreshEntity(display);
                        }
                    }

                    if (result.isValidLeaves) {
                        foundLeaves = true;
                        if (validLeaves == null) {
                            validLeaves = IdTagMatcher.newId(ForgeRegistries.BLOCKS.getKey(neighborState.getBlock()).toString());
                        }
                    }

                    if (result.isSameLog) {
                        if (neighborState.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y) {
                            logs++;
                        } else {
                            sidewaysLogs++;
                        }
                        maxY = Math.max(maxY, neighborImmutable.getY());
                    }
                }
            }

            // Check if we should early exit
            if (posToCheck.isEmpty() && !isValidTree(foundLeaves, logs, sidewaysLogs, treeInfo)) {
                return new ArrayList<>();
            }
        }

        // Final validation
        if (maxY < pos.getY() + 2) {
            return new ArrayList<>();
        }

        return new ArrayList<>(visitedBlocks);
    }

    private static boolean initializeTreeSearch(BlockPos pos, Level level, IdTagMatcher validLogs,
                                                 Set<BlockPos> visitedBlocks, Queue<BlockPos> posToCheck) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);

        // Check block directly above
        if (validLogs.matchesBlock(aboveState)) {
            return true;
        }

        // Check blocks around the one above
        for (Direction dir : XZ_DIRECTIONS) {
            BlockPos checkPos = above.relative(dir);
            if (validLogs.matchesBlock(level.getBlockState(checkPos))) {
                return true;
            }
        }

        return false;
    }

    private static BlockCheckResult checkBlock(BlockPos pos, BlockState state, BlockState currentState,
                                                BlockPos originPos, IdTagMatcher validLogs,
                                                IdTagMatcher validLeaves, TreeInfo treeInfo) {
        BlockCheckResult result = new BlockCheckResult();

        // Check attached blocks (like vines)
        if (state.is(ATTACHED_BLOCKS)) {
            result.shouldAdd = true;
            result.isAttached = true;
            return result;
        }

        boolean isLeaves = state.is(BlockTags.LEAVES);
        result.isSameLog = validLogs.matchesBlock(state);

        // Check if it's valid leaves
        if (isLeaves && validLeaves != null) {
            result.isValidLeaves = validLeaves.matchesBlock(state.getBlock())
                && !state.getValue(LeavesBlock.PERSISTENT);
        } else if (isLeaves && validLeaves == null) {
            result.isValidLeaves = true;
        }

        // Must be either a valid log or valid leaves
        if (!result.isSameLog && !result.isValidLeaves) {
            return result;
        }

        // Check distance constraint
        if (xzDistance(pos, originPos) > treeInfo.maxDistanceFromLogs) {
            return result;
        }

        // Check leaf distance property
        if (isLeaves && currentState.is(BlockTags.LEAVES)) {
            int currentDistance = currentState.getValue(LeavesBlock.DISTANCE);
            int newDistance = state.getValue(LeavesBlock.DISTANCE);
            if (newDistance <= currentDistance && newDistance != 7) {
                return result;
            }
        }

        result.shouldAdd = true;
        return result;
    }

    private static boolean isValidTree(boolean foundLeaves, int logs, int sidewaysLogs, TreeInfo treeInfo) {
        if (!foundLeaves) return false;
        if (logs + sidewaysLogs < treeInfo.minLogs) return false;

        float logsSidewaysRatio = sidewaysLogs == 0 ? 999 : (float) logs / sidewaysLogs;
        return logsSidewaysRatio >= treeInfo.logsSidewaysRatio;
    }

    private static class BlockCheckResult {
        boolean shouldAdd = false;
        boolean isAttached = false;
        boolean isValidLeaves = false;
        boolean isSameLog = false;
    }

    public static final List<Direction> XZ_DIRECTIONS = List.of(Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST);
    public static final List<Direction> DIRECTIONS = List.of(Direction.UP, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST);
    public static Iterable<BlockPos> getPositionsToCheck(BlockPos pos, BlockState state) {
        if (state.is(BlockTags.LOGS))
            return BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 1, 1));
        else
        {
            List<BlockPos> positions = new ArrayList<>();
            for (Direction d : Direction.values()) {
                positions.add(pos.relative(d));
            }
            return positions;
        }
    }

    public static int xzDistance(BlockPos pos1, BlockPos pos2) {
        return pos1.atY(pos2.getY()).distManhattan(pos2);
    }

    public static BlockState rotatePillar(BlockState state, Direction.Axis playerAxis) {
		Direction.Axis logAxis = state.getValue(RotatedPillarBlock.AXIS);
		if (logAxis == Direction.Axis.Y)
			return state.setValue(RotatedPillarBlock.AXIS, playerAxis);
		return state;
    }
}
