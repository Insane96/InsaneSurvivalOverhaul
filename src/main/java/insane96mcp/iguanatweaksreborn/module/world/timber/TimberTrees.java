package insane96mcp.iguanatweaksreborn.module.world.timber;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.data.generator.ISOBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.entity.ISOFallingBlockEntity;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@Label(name = "Timber Trees", description = "Trees fall when cut.")
@LoadFeature(module = Modules.Ids.WORLD)
public class TimberTrees extends JsonFeature {

    public static final TagKey<Block> TIMBER_TRUNKS = ISOBlockTagsProvider.create("timber_trunks");

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
            new TreeInfo.Builder().log(IdTagMatcher.newId("environmental:pine_log")).leaves(IdTagMatcher.newId("environmental:pine_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("environmental:plum_log")).leaves(IdTagMatcher.newId("environmental:plum_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("environmental:willow_log")).leaves(IdTagMatcher.newId("environmental:willow_leaves")).build(),
            new TreeInfo.Builder().log(IdTagMatcher.newId("environmental:wisteria_log")).leaves(IdTagMatcher.newTag("iguanatweaksreborn:wisteria_leaves")).build()
    ));
    public static final ArrayList<TreeInfo> treeInfos = new ArrayList<>();

    @Config
    @Label(name = "Requires #minecraft:axes", description = "Trees will timber only if an item in the minecraft:axes item tag is used")
    public static Boolean requiresAxe = false;

    public TimberTrees(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        JSON_CONFIGS.add(new JsonConfig<>("tree_info.json", treeInfos, TREE_INFOS_DEFAULT, TreeInfo.LIST_TYPE));
    }

    @Override
    public String getModConfigFolder() {
        return InsaneSurvivalOverhaul.CONFIG_FOLDER;
    }

    @SubscribeEvent
    public void onLogBreak(BlockEvent.BreakEvent event) {
        if (!this.isEnabled()
            || !event.getState().is(TIMBER_TRUNKS)
            || !(event.getState().getBlock() instanceof RotatedPillarBlock)
            || event.getState().getValue(RotatedPillarBlock.AXIS) != Direction.Axis.Y
            || (requiresAxe && !(event.getPlayer().getMainHandItem().is(ItemTags.AXES))))
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
            BlockPos fallingBlockPos = pos.relative(direction, verticalDistance + horizontalDistance).above(horizontalDistance);
            BlockState state = level.getBlockState(pos);
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
            ISOFallingBlockEntity fallingBlock = new ISOFallingBlockEntity(level, fallingBlockPos, state, direction);
            fallingBlock.move(MoverType.SELF, new Vec3(0, 0.1d * horizontalDistance, 0));
            if (state.is(TIMBER_TRUNKS))
                fallingBlock.setHurtsEntities(0.5f, 20);
            else
                fallingBlock.setHurtsEntities(0.1f, 4);
            level.addFreshEntity(fallingBlock);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static List<BlockPos> getTreeBlocks(BlockPos pos, BlockState state, Level level, TreeInfo treeInfo) {
        List<BlockPos> blocks = new ArrayList<>();
        boolean foundLeaves = false;
        int checks = 0;
        int logs = 0;
        int sidewaysLogs = 0;
        int maxY = pos.getY();
        IdTagMatcher validLogs = Objects.requireNonNullElseGet(treeInfo.log, () -> IdTagMatcher.newId(ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString()));
        //Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> posToCheck = new ArrayDeque<>(); //Queue seem to be faster than ArrayList
        //Get the block above
        posToCheck.add(pos.above());
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos().set(pos);
        BlockState stateToCheck = level.getBlockState(pos.above());
        //If the block above is not the same log, check the blocks around
        if (!validLogs.matchesBlock(stateToCheck)) {
            for (Direction dir : XZ_DIRECTIONS) {
                stateToCheck = level.getBlockState(pos.above().relative(dir));
                if (validLogs.matchesBlock(stateToCheck)) {
                    blocks.add(blockPos.immutable());
                    posToCheck.add(blockPos.immutable());
                    logs++;
                    break;
                }
            }
        }
        else
            logs++;

        if (logs == 0)
            return blocks;

        blocks.add(blockPos.immutable());
        posToCheck.add(blockPos.immutable());
        IdTagMatcher validLeaves = null;
        if (treeInfo.leaves != null)
            validLeaves = treeInfo.leaves;
        int i = 0;
        while (checks < 1536 && !posToCheck.isEmpty()) {
            List<BlockPos> posToCheckTmp = new ArrayList<>(posToCheck);
            posToCheck.clear();
            for (BlockPos p : posToCheckTmp) {
                BlockState currState = level.getBlockState(p);
                Iterable<BlockPos> positionsToLoop = getPositionsToCheck(p, currState);
                for (BlockPos positionToLoop : positionsToLoop) {
                    blockPos.set(positionToLoop);
                    stateToCheck = level.getBlockState(blockPos);
                    if (stateToCheck.isAir())
                        continue;
                    //if (visited.contains(positionToLoop))
                        //continue;
                    //visited.add(positionToLoop);
                    BlockPos posImmutable = blockPos.immutable();
                    if (blocks.contains(posImmutable))
                        continue;
                    boolean isLeaves = stateToCheck.is(BlockTags.LEAVES);
                    boolean isValidLeaves = validLeaves != null && validLeaves.matchesBlock(stateToCheck.getBlock()) && !stateToCheck.getValue(LeavesBlock.PERSISTENT);
                    boolean isSameLog = validLogs.matchesBlock(stateToCheck);
                    boolean isInDistance = xzDistance(posImmutable, pos) <= treeInfo.maxDistanceFromLogs;
                    boolean isCurrLeaves = currState.is(BlockTags.LEAVES);
                    boolean isCorrectLeavesDistance = isLeaves && isCurrLeaves && (stateToCheck.getValue(LeavesBlock.DISTANCE) > currState.getValue(LeavesBlock.DISTANCE) || stateToCheck.getValue(LeavesBlock.DISTANCE) == 7);
                    if (isLeaves && validLeaves == null) {
                        validLeaves = IdTagMatcher.newId(ForgeRegistries.BLOCKS.getKey(stateToCheck.getBlock()).toString());
                        isValidLeaves = true;
                    }
                    if ((isSameLog || isValidLeaves) && isInDistance && (!isLeaves || !isCurrLeaves || isCorrectLeavesDistance)) {
                        blocks.add(posImmutable);
                        //level.removeBlock(posImmutable, false);
                        posToCheck.add(posImmutable);
                        if (!FMLLoader.isProduction()) {
                            //Display.TextDisplay display = EntityType.TEXT_DISPLAY.create((Level) level);
                            //display.setPos(posImmutable.getCenter());
                            //display.setText(Component.literal(i++ + ""));
                            //level.addFreshEntity(display);
                        }
                        if (isValidLeaves)
                            foundLeaves = true;
                        if (isSameLog) {
                            if (stateToCheck.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y)
                                logs++;
                            else
                                sidewaysLogs++;

                            if (posImmutable.getY() > maxY)
                                maxY = posImmutable.getY();
                        }
                    }
                }
                checks++;
            }
            float logsSidewaysRatio = sidewaysLogs == 0 ? 999 : (float) logs / sidewaysLogs;
            if (posToCheck.isEmpty()
                    && (!foundLeaves
                        || logs + sidewaysLogs < treeInfo.minLogs
                        || logsSidewaysRatio < treeInfo.logsSidewaysRatio)) {
                blocks.clear();
                break;
            }
        }
        if (maxY < pos.getY() + 2)
            blocks.clear();
        return blocks;
    }

    public static final List<Direction> XZ_DIRECTIONS = List.of(Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST);
    public static final List<Direction> DIRECTIONS = List.of(Direction.UP, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST);
    public static Iterable<BlockPos> getPositionsToCheck(BlockPos pos, BlockState state) {
        if (state.is(BlockTags.LOGS))
            return BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 1, 1));
        else
        {
            List<BlockPos> positions = new ArrayList<>();
            for (Direction d : DIRECTIONS) {
                positions.add(pos.relative(d));
            }
            return positions;
        }
    }

    public static int xzDistance(BlockPos pos1, BlockPos pos2) {
        return pos1.atY(pos2.getY()).distManhattan(pos2);
    }

    public static BlockState rotatePillar(BlockState state, Direction.Axis axis) {
        switch (axis) {
            case X -> {
                return switch (state.getValue(RotatedPillarBlock.AXIS)) {
                    case X -> state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
                    case Y -> state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
                    default -> state;
                };
            }
            case Z -> {
                return switch (state.getValue(RotatedPillarBlock.AXIS)) {
                    case Y -> state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
                    case Z -> state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
                    default -> state;
                };
            }
        }
        return state;
    }
}
