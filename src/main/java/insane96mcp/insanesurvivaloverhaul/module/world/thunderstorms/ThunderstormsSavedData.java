package insane96mcp.insanesurvivaloverhaul.module.world.thunderstorms;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class ThunderstormsSavedData extends SavedData {

    public ThunderIntensityData thunderIntensityData = new ThunderIntensityData(0, -1, 5);

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag compoundTag, @NotNull HolderLookup.Provider registries) {
        CompoundTag thunderIntensityDataTag = new CompoundTag();
        thunderIntensityData.save(thunderIntensityDataTag);
        compoundTag.put("thunder_intensity_data", thunderIntensityDataTag);
        return compoundTag;
    }

    public static ThunderstormsSavedData create() {
        return new ThunderstormsSavedData();
    }

    public static ThunderstormsSavedData load(CompoundTag compoundTag, HolderLookup.Provider lookupProvider) {
        ThunderstormsSavedData data = new ThunderstormsSavedData();
        data.thunderIntensityData.load(compoundTag.getCompound("thunder_intensity_data"));
        return data;
    }

    public static ThunderstormsSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ThunderstormsSavedData::create, ThunderstormsSavedData::load),
                InsaneSO.MOD_ID + "_thunderstorms");
    }

    public static final class ThunderIntensityData {
        public int timer;
        public int targetIntensity;
        private int intensity;

        public ThunderIntensityData(int timer, int targetIntensity, int intensity) {
            this.timer = timer;
            this.targetIntensity = targetIntensity;
            this.intensity = intensity;
        }

        public void save(CompoundTag compoundTag) {
            compoundTag.putInt("timer", this.timer);
            compoundTag.putInt("targetIntensity", this.targetIntensity);
            compoundTag.putInt("intensity", this.intensity <= 0 ? 1 : this.intensity);
        }

        public void load(CompoundTag compoundTag) {
            if (compoundTag == null)
                return;
            this.timer = compoundTag.getInt("timer");
            this.targetIntensity = compoundTag.getInt("targetIntensity");
            this.intensity = compoundTag.getInt("intensity");
        }

        public int getIntensity() {
            return intensity <= 0 ? 1 : intensity;
        }

        public void addIntensity(int amount) {
            this.intensity += amount;
        }

        @Override
        public String toString() {
            return "ThunderIntensityData{" +
                    "timer=" + timer +
                    ", targetIntensity=" + targetIntensity +
                    ", intensity=" + intensity +
                    '}';
        }
    }
}