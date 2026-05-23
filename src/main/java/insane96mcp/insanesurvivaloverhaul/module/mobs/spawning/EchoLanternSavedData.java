package insane96mcp.insanesurvivaloverhaul.module.mobs.spawning;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

public class EchoLanternSavedData extends SavedData {
    public static final String ID = "echo_lantern_positions";

    public static final Factory<EchoLanternSavedData> FACTORY = new Factory<>(
            EchoLanternSavedData::new,
            EchoLanternSavedData::load,
            null
    );

    private final Set<BlockPos> positions = new HashSet<>();

    public static EchoLanternSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, ID);
    }

    public boolean isNearLantern(BlockPos pos, double radius) {
        double radiusSq = radius * radius;
        for (BlockPos lanternPos : positions) {
            if (lanternPos.distSqr(pos) <= radiusSq)
                return true;
        }
        return false;
    }

    public void addPosition(BlockPos pos) {
        positions.add(pos.immutable());
        setDirty();
    }

    public void removePosition(BlockPos pos) {
        if (positions.remove(pos))
            setDirty();
    }

    private static EchoLanternSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        EchoLanternSavedData data = new EchoLanternSavedData();
        ListTag list = tag.getList("positions", Tag.TAG_LONG);
        for (Tag t : list) {
            data.positions.add(BlockPos.of(((LongTag) t).getAsLong()));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (BlockPos pos : positions) {
            list.add(LongTag.valueOf(pos.asLong()));
        }
        tag.put("positions", list);
        return tag;
    }
}
