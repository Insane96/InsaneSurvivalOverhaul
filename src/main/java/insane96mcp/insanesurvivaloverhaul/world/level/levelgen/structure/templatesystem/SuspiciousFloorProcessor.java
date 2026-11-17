package insane96mcp.insanesurvivaloverhaul.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Replaces a random number of covered floor blocks of the piece with a brushable block (e.g. suspicious gravel) holding a loot table.
 * A covered floor block is a full block with nothing (or air) above it, a block somewhere higher in the same column (a roof) and a full block below it
 * (or the bottom of the piece), so the brushable block doesn't fall and is inside a building.
 * Unlike a {@code rule} processor, it sees the whole piece, so it can find floors whatever they are made of.
 * The choice uses the same random as {@code capped}, so it's the same in every chunk the piece is placed in.
 */
public class SuspiciousFloorProcessor extends StructureProcessor {
    public static final MapCodec<SuspiciousFloorProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("state").forGetter(processor -> processor.state),
            ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").forGetter(processor -> processor.lootTable),
            IntProvider.NON_NEGATIVE_CODEC.fieldOf("count").forGetter(processor -> processor.count)
    ).apply(instance, SuspiciousFloorProcessor::new));

    private final BlockState state;
    private final ResourceKey<LootTable> lootTable;
    private final IntProvider count;

    public SuspiciousFloorProcessor(BlockState state, ResourceKey<LootTable> lootTable, IntProvider count) {
        this.state = state;
        this.lootTable = lootTable;
        this.count = count;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ISORegistries.SUSPICIOUS_FLOOR_PROCESSOR.get();
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor level, BlockPos offset, BlockPos pos, List<StructureTemplate.StructureBlockInfo> originalBlockInfos, List<StructureTemplate.StructureBlockInfo> processedBlockInfos, StructurePlaceSettings settings) {
        if (processedBlockInfos.isEmpty() || this.count.getMaxValue() <= 0)
            return processedBlockInfos;

        //Legacy pool elements (villages) drop the air blocks, so a missing position counts as air
        Object2IntOpenHashMap<BlockPos> indexes = new Object2IntOpenHashMap<>();
        indexes.defaultReturnValue(-1);
        Long2IntOpenHashMap topY = new Long2IntOpenHashMap();
        topY.defaultReturnValue(Integer.MIN_VALUE);
        int minY = Integer.MAX_VALUE;
        for (int i = 0; i < processedBlockInfos.size(); i++) {
            StructureTemplate.StructureBlockInfo info = processedBlockInfos.get(i);
            indexes.put(info.pos(), i);
            minY = Math.min(minY, info.pos().getY());
            if (!info.state().isAir()) {
                long column = columnKey(info.pos());
                topY.put(column, Math.max(topY.get(column), info.pos().getY()));
            }
        }

        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < processedBlockInfos.size(); i++) {
            StructureTemplate.StructureBlockInfo info = processedBlockInfos.get(i);
            BlockPos blockPos = info.pos();
            if (info.nbt() != null
                    || !isFullBlock(info.state())
                    || topY.get(columnKey(blockPos)) <= blockPos.getY() + 1)
                continue;
            int above = indexes.getInt(blockPos.above());
            if (above != -1 && !processedBlockInfos.get(above).state().isAir())
                continue;
            if (blockPos.getY() > minY) {
                int below = indexes.getInt(blockPos.below());
                if (below == -1 || !isFullBlock(processedBlockInfos.get(below).state()))
                    continue;
            }
            candidates.add(i);
        }
        if (candidates.isEmpty())
            return processedBlockInfos;

        RandomSource random = RandomSource.create(level.getLevel().getSeed()).forkPositional().at(offset);
        int amount = Math.min(this.count.sample(random), candidates.size());
        Util.shuffle(candidates, random);
        for (int c = 0; c < amount; c++) {
            int i = candidates.get(c);
            CompoundTag nbt = new CompoundTag();
            nbt.putString("LootTable", this.lootTable.location().toString());
            nbt.putLong("LootTableSeed", random.nextLong());
            processedBlockInfos.set(i, new StructureTemplate.StructureBlockInfo(processedBlockInfos.get(i).pos(), this.state, nbt));
        }
        return processedBlockInfos;
    }

    private static boolean isFullBlock(BlockState state) {
        return state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }

    private static long columnKey(BlockPos pos) {
        return BlockPos.asLong(pos.getX(), 0, pos.getZ());
    }
}
