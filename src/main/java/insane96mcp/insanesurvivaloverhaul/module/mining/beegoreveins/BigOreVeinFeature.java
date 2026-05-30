package insane96mcp.insanesurvivaloverhaul.module.mining.beegoreveins;

import com.mojang.serialization.Codec;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.ChunkStepAccessor;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.WorldGenRegionAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import java.util.function.Function;

public class BigOreVeinFeature extends Feature<OreWithRandomPatchConfiguration> {

    public BigOreVeinFeature(Codec<OreWithRandomPatchConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreWithRandomPatchConfiguration> context) {
        RandomSource randomSource = context.random();
        BlockPos blockPos = context.origin();
        WorldGenLevel worldGenLevel = context.level();
        int originalBlockStateWriteRadius = -1;
        if (worldGenLevel instanceof WorldGenRegion worldGenRegion) {
            originalBlockStateWriteRadius = ((WorldGenRegionAccessor) worldGenRegion).getGeneratingStep().blockStateWriteRadius();
            setBlockStateWriteRadius(worldGenRegion, 3);
        }
        OreWithRandomPatchConfiguration configuration = context.config();

        int radius = configuration.radius.sample(randomSource);
        int radiusSq = radius * radius;

        int placed = 0;
        try (BulkSectionAccess bulksectionaccess = new BulkSectionAccess(worldGenLevel)) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        int distanceSquared = dx * dx + dy * dy + dz * dz;
                        if (distanceSquared > radiusSq)
                            continue;
                        if (configuration.centerFalloff > 0.0F) {
                            float normDist = (float) Math.sqrt(distanceSquared) / radius;
                            if (randomSource.nextFloat() < normDist * configuration.centerFalloff)
                                continue;
                        }
                        mutableBlockPos.set(blockPos.getX() + dx, blockPos.getY() + dy, blockPos.getZ() + dz);
                        if (worldGenLevel.ensureCanWrite(mutableBlockPos)) {
                            LevelChunkSection levelchunksection = bulksectionaccess.getSection(mutableBlockPos);
                            if (levelchunksection != null) {
                                int i3 = SectionPos.sectionRelative(mutableBlockPos.getX());
                                int j3 = SectionPos.sectionRelative(mutableBlockPos.getY());
                                int k3 = SectionPos.sectionRelative(mutableBlockPos.getZ());
                                BlockState blockstate = levelchunksection.getBlockState(i3, j3, k3);

                                for (OreConfiguration.TargetBlockState oreconfiguration$targetblockstate : configuration.targetStates) {
                                    if (canPlaceOre(blockstate, bulksectionaccess::getBlockState, randomSource, configuration, oreconfiguration$targetblockstate, mutableBlockPos)) {
                                        levelchunksection.setBlockState(i3, j3, k3, oreconfiguration$targetblockstate.state, false);
                                        placed++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (placed == 0) {
            if (originalBlockStateWriteRadius > -1)
                setBlockStateWriteRadius((WorldGenRegion) worldGenLevel, originalBlockStateWriteRadius);
            return false;
        }
        int randomPatchToPlace = placed / configuration.patchConfiguration.tries();
        int placedRandomPatch = 0;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int xzSpread = configuration.patchConfiguration.xzSpread() + 1;
        int ySpread = configuration.patchConfiguration.ySpread() + 1;

        for (int m = 0; m < randomPatchToPlace; ++m) {
            blockpos$mutableblockpos.setWithOffset(blockPos, randomSource.nextInt(xzSpread) - randomSource.nextInt(xzSpread), randomSource.nextInt(ySpread) - randomSource.nextInt(ySpread), randomSource.nextInt(xzSpread) - randomSource.nextInt(xzSpread));
            if (configuration.patchConfiguration.feature().value().place(worldGenLevel, context.chunkGenerator(), randomSource, blockpos$mutableblockpos)) {
                ++placedRandomPatch;
            }
        }

        if (originalBlockStateWriteRadius > -1)
            setBlockStateWriteRadius((WorldGenRegion) worldGenLevel, originalBlockStateWriteRadius);
        return placedRandomPatch > 0;
    }

    public static boolean canPlaceOre(BlockState state, Function<BlockPos, BlockState> func, RandomSource random, OreWithRandomPatchConfiguration configuration, OreConfiguration.TargetBlockState targetBlockState, BlockPos.MutableBlockPos mutableBlockPos) {
        if (!targetBlockState.target.test(state, random)) {
            return false;
        } else if (shouldSkipAirCheck(random, configuration.discardChanceOnAirExposure)) {
            return true;
        } else {
            return !isAdjacentToAir(func, mutableBlockPos);
        }
    }

    protected static boolean shouldSkipAirCheck(RandomSource random, float discardChance) {
        if (discardChance <= 0.0F) {
            return true;
        } else if (discardChance >= 1.0F) {
            return false;
        } else {
            return random.nextFloat() >= discardChance;
        }
    }

    private static void setBlockStateWriteRadius(WorldGenRegion worldGenRegion, int blockStateWriteRadius) {
        ((ChunkStepAccessor) (Object) ((WorldGenRegionAccessor) worldGenRegion).getGeneratingStep()).setBlockStateWriteRadius(blockStateWriteRadius);
    }
}
