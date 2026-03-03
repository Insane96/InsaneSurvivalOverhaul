package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks;

import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.world.level.block.SpongeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SpongeBlock.class)
public class SpongeBlockMixin_Tweaks {
    /**
     * Replaces the hardcoded maximum number of water blocks a sponge can absorb (65) with
     * the configured value from {@link Tweaks#changeMaxSpongeSoakBlocks}.
     */
    @ModifyConstant(method = "removeWaterBreadthFirstSearch", constant = @Constant(intValue = 65))
    public int insanesurvivaloverhaul$onSpongeDrainLimit(int limit) {
        return Tweaks.changeMaxSpongeSoakBlocks(limit);
    }

    /**
     * Replaces the hardcoded sponge absorption radius (6 blocks) with the configured value
     * from {@link Tweaks#changeSpongeMaxRange}.
     */
    @ModifyConstant(method = "removeWaterBreadthFirstSearch", constant = @Constant(intValue = 6))
    public int insanesurvivaloverhaul$onSpongeCrawlRange(int range) {
        return Tweaks.changeSpongeMaxRange(range);
    }
}
