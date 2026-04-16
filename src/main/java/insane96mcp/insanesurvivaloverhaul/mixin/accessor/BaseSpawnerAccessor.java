package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BaseSpawner.class)
public interface BaseSpawnerAccessor {
    @Accessor
    int getSpawnDelay();
    @Accessor
    void setSpawnDelay(int spawnDelay);

    @Accessor
    int getRequiredPlayerRange();
    @Accessor
    void setRequiredPlayerRange(int requiredPlayerRange);

    @Nullable
    @Accessor
    SpawnData getNextSpawnData();
}
