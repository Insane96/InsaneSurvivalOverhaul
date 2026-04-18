package insane96mcp.insanesurvivaloverhaul.module.mining.beegoreveins;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
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
        RandomSource randomsource = context.random();
        BlockPos blockpos = context.origin();
        WorldGenLevel worldgenlevel = context.level();
        OreWithRandomPatchConfiguration configuration = context.config();

        int radius = configuration.radius.sample(randomsource);
        int radiusSq = radius * radius;

        int placed = 0;
        try (BulkSectionAccess bulksectionaccess = new BulkSectionAccess(worldgenlevel)) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (dx * dx + dy * dy + dz * dz > radiusSq)
                            continue;
                        if (configuration.centerFalloff > 0.0F) {
                            float normDist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) / radius;
                            if (randomsource.nextFloat() < normDist * configuration.centerFalloff)
                                continue;
                        }
                        mutableBlockPos.set(blockpos.getX() + dx, blockpos.getY() + dy, blockpos.getZ() + dz);
                        if (worldgenlevel.ensureCanWrite(mutableBlockPos)) {
                            LevelChunkSection levelchunksection = bulksectionaccess.getSection(mutableBlockPos);
                            if (levelchunksection != null) {
                                int i3 = SectionPos.sectionRelative(mutableBlockPos.getX());
                                int j3 = SectionPos.sectionRelative(mutableBlockPos.getY());
                                int k3 = SectionPos.sectionRelative(mutableBlockPos.getZ());
                                BlockState blockstate = levelchunksection.getBlockState(i3, j3, k3);

                                for (OreConfiguration.TargetBlockState oreconfiguration$targetblockstate : configuration.targetStates) {
                                    if (canPlaceOre(blockstate, bulksectionaccess::getBlockState, randomsource, configuration, oreconfiguration$targetblockstate, mutableBlockPos)) {
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

        if (placed == 0)
            return false;
        int randomPatchToPlace = placed / configuration.patchConfiguration.tries();
        int placedRandomPatch = 0;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int xzSpread = configuration.patchConfiguration.xzSpread() + 1;
        int ySpread = configuration.patchConfiguration.ySpread() + 1;

        for (int m = 0; m < randomPatchToPlace; ++m) {
            blockpos$mutableblockpos.setWithOffset(blockpos, randomsource.nextInt(xzSpread) - randomsource.nextInt(xzSpread), randomsource.nextInt(ySpread) - randomsource.nextInt(ySpread), randomsource.nextInt(xzSpread) - randomsource.nextInt(xzSpread));
            if (configuration.patchConfiguration.feature().value().place(worldgenlevel, context.chunkGenerator(), randomsource, blockpos$mutableblockpos)) {
                ++placedRandomPatch;
            }
        }

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
}
