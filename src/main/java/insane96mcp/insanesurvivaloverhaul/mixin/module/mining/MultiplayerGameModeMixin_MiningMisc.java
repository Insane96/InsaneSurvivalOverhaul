package insane96mcp.insanesurvivaloverhaul.mixin.module.mining;

import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanesurvivaloverhaul.module.mining.MiningMisc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiplayerGameModeMixin_MiningMisc {
    @Shadow @Final
    private Minecraft minecraft;

    /**
     * Changes the destroy delay in creative to a fixed value
     */
    @ModifyConstant(method = {"startDestroyBlock", "continueDestroyBlock"}, constant = @Constant(intValue = 5, ordinal = 0))
    private int insanesurvivaloverhaul$changeCreativeDestroyDelay(int destroyDelay) {
        return MiningMisc.creativeDestroyDelay();
    }

    /**
     * Changes the destroy delay after a block has been broken (non-instantly) based on the mining speed. Uses the state captured before the block was broken
     */
    @SuppressWarnings("DataFlowIssue")
    @ModifyConstant(method = "continueDestroyBlock", constant = @Constant(intValue = 5, ordinal = 1))
    private int insanesurvivaloverhaul$changeDestroyDelay(int destroyDelay, @Local BlockState state) {
        return MiningMisc.destroyDelay(this.minecraft.player, state);
    }
}
