package insane96mcp.iguanatweaksreborn.mixin;

import net.minecraft.world.entity.npc.VillagerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerData.class)
public interface VillagerDataAccessor {
    @Accessor("NEXT_LEVEL_XP_THRESHOLDS")
    @Mutable
    static void setNextLevelXpThresholds(int[] nextLevelXpThresholds) {
        throw new AssertionError();
    };
}
