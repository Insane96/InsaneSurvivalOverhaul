package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.mining.MiningMisc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiplayerGameModeMixin {

    @Shadow private int destroyDelay;

    @Shadow private GameType localPlayerMode;

    @Shadow @Final private Minecraft minecraft;

    @Shadow private BlockPos destroyBlockPos;

    @Shadow private ItemStack destroyingItem;

    @Shadow public abstract boolean destroyBlock(BlockPos pPos);

    @SuppressWarnings("DataFlowIssue")
    @ModifyConstant(method = "continueDestroyBlock", constant = @Constant(intValue = 5))
    private int changeDestroyDelay(int destroyDelay, BlockPos pos, Direction facingDirection) {
        if (!(this.minecraft.player.getMainHandItem().getItem() instanceof DiggerItem diggerItem))
            return destroyDelay;
        return MiningMisc.destroyDelay(this.minecraft.player.getMainHandItem(), diggerItem, this.minecraft.level.getBlockState(pos));
    }

    //Fixes https://github.com/neoforged/NeoForge/issues/143
    // TODO Remove in 1.20.2+
    @Inject(method = "sameDestroyTarget", at = @At("HEAD"), cancellable = true)
    private void fixHealingItemsResettingBreaking(BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        ItemStack itemstack = this.minecraft.player.getMainHandItem();
        cir.setReturnValue(pPos.equals(this.destroyBlockPos) && !destroyingItem.shouldCauseBlockBreakReset(itemstack));
    }
}
