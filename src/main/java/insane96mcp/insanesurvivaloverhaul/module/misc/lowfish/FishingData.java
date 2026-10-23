package insane96mcp.insanesurvivaloverhaul.module.misc.lowfish;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FishingData extends SavedData {
    List<Entry> entries;

    public FishingData() {
        this.entries = new ArrayList<>();
    }

    private static final long EXPIRE_TIME = 36000; //30 minutes

    /**
     * Removes all the fishing spots not fished in a while, so they're never found (and incremented) again
     */
    private void removeExpired(long gameTime) {
        if (this.entries.removeIf(entry -> gameTime - entry.lastFished > EXPIRE_TIME))
            this.setDirty();
    }

    public int getCountForPos(BlockPos pos, long gameTime) {
        this.removeExpired(gameTime);
        int range = LowFish.fishingSpotRange * LowFish.fishingSpotRange;
        for (Entry entry : this.entries) {
            if (entry.pos.distSqr(pos) <= range)
                return entry.count;
        }
        return 0;
    }

    public void addOrIncrementPos(BlockPos pos, long gameTime) {
        this.removeExpired(gameTime);
        int range = LowFish.fishingSpotRange * LowFish.fishingSpotRange;
        for (Entry entry : this.entries) {
            if (entry.pos.distSqr(pos) <= range) {
                entry.increment(gameTime);
                return;
            }
        }
        this.entries.add(new Entry(pos, 1, gameTime));
    }

    @Override
    @NotNull
    public CompoundTag save(CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        this.entries.forEach(entry -> {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putLong("pos", entry.pos.asLong());
            compoundTag.putInt("count", entry.count);
            compoundTag.putLong("lastFished", entry.lastFished);
            listTag.add(compoundTag);
        });
        tag.put("fishedPos", listTag);
        return tag;
    }

    public static FishingData create() {
        return new FishingData();
    }

    public static FishingData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        FishingData data = new FishingData();
        tag.getList("fishedPos", Tag.TAG_COMPOUND).forEach(compound -> {
            BlockPos pos = BlockPos.of(((CompoundTag) compound).getLong("pos"));
            int count = ((CompoundTag) compound).getInt("count");
            long lastFished = ((CompoundTag) compound).getLong("lastFished");
            data.entries.add(new Entry(pos, count, lastFished));
        });

        return data;
    }

    public static class Entry {
        BlockPos pos;
        int count;
        long lastFished;

        public Entry(BlockPos pos, int count, long lastFished) {
            this.pos = pos;
            this.count = count;
            this.lastFished = lastFished;
        }

        public void increment(long gameTime) {
            this.count++;
            this.lastFished = gameTime;
        }
    }
}
