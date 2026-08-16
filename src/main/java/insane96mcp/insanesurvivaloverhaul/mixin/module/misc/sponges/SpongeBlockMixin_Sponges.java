package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.sponges;

import insane96mcp.insanesurvivaloverhaul.module.misc.Sponges;
import net.minecraft.world.level.block.SpongeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SpongeBlock.class)
public class SpongeBlockMixin_Sponges {
    /**
     * Replaces the hardcoded maximum number of water blocks a sponge can absorb (65) with
     * the configured value from {@link Sponges#changeMaxSpongeSoakBlocks}.
     */
    @ModifyConstant(method = "removeWaterBreadthFirstSearch", constant = @Constant(intValue = 65))
    public int insanesurvivaloverhaul$onSpongeDrainLimit(int limit) {
        return Sponges.changeMaxSpongeSoakBlocks(limit);
    }

    /**
     * Replaces the hardcoded sponge absorption radius (6 blocks) with the configured value
     * from {@link Sponges#changeSpongeMaxRange}.
     */
    @ModifyConstant(method = "removeWaterBreadthFirstSearch", constant = @Constant(intValue = 6))
    public int insanesurvivaloverhaul$onSpongeCrawlRange(int range) {
        return Sponges.changeSpongeMaxRange(range);
    }
}
