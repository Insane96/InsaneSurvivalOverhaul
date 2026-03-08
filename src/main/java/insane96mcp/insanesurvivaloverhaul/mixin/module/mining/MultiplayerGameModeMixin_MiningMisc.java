package insane96mcp.insanesurvivaloverhaul.mixin.module.mining;

import insane96mcp.insanesurvivaloverhaul.module.mining.MiningMisc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DiggerItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiplayerGameModeMixin_MiningMisc {
    @Shadow @Final
    private Minecraft minecraft;

    @SuppressWarnings("DataFlowIssue")
    @ModifyConstant(method = "continueDestroyBlock", constant = @Constant(intValue = 5))
    private int changeDestroyDelay(int destroyDelay, BlockPos pos, Direction facingDirection) {
        if (!(this.minecraft.player.getMainHandItem().getItem() instanceof DiggerItem diggerItem))
            return destroyDelay;
        return MiningMisc.destroyDelay(this.minecraft.player.getMainHandItem(), diggerItem, this.minecraft.level.getBlockState(pos));
    }
}
