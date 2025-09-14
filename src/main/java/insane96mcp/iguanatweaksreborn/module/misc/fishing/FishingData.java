package insane96mcp.iguanatweaksreborn.module.misc.fishing;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class FishingData extends SavedData {
    Map<BlockPos, Integer> posCount;

    public FishingData() {
        this.posCount = new HashMap<>();
    }

    public int getCountForPos(BlockPos pos) {
        if (!this.posCount.containsKey(pos)) {
            for (var pc : this.posCount.entrySet()) {
                if (pc.getKey().distSqr(pos) <= 49d) {
                    return this.posCount.getOrDefault(pos, 0);
                }
            }
        }
        return 0;
    }

    public void addOrIncrementPos(BlockPos pos) {
        if (!this.posCount.containsKey(pos)) {
            for (var pc : this.posCount.entrySet()) {
                if (pc.getKey().distSqr(pos) <= 49d) {
                    this.posCount.put(pc.getKey(), pc.getValue() + 1);
                    return;
                }
            }
        }
        this.posCount.put(pos, 1);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag listTag = new ListTag();
        this.posCount.forEach((pos, count) -> {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putLong("pos", pos.asLong());
            compoundTag.putInt("count", count);
            listTag.add(compoundTag);
        });
        tag.put("fishedPos", listTag);
        return tag;
    }

    public static FishingData create() {
        return new FishingData();
    }

    public static FishingData load(CompoundTag tag) {
        FishingData data = new FishingData();
        tag.getList("fishedPos", Tag.TAG_COMPOUND).forEach(compound -> {
            BlockPos pos = BlockPos.of(((CompoundTag) compound).getLong("pos"));
            int count = ((CompoundTag) compound).getInt("count");
            data.posCount.put(pos, count);
        });

        return data;
    }
}
