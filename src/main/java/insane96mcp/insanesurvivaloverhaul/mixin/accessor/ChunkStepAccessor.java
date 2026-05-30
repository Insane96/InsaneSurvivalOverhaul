package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.world.level.chunk.status.ChunkStep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkStep.class)
public interface ChunkStepAccessor {
    @Accessor
    @Mutable
    void setBlockStateWriteRadius(int blockStateWriteRadius);
}
