package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.conduit;

import insane96mcp.insanesurvivaloverhaul.module.misc.Conduit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ConduitBlockEntity.class)
public class ConduitBlockEntityMixin_Conduit {

    /**
     * Replaces vanilla's fixed-damage/fixed-range destroy target logic with {@link Conduit#conduitUpdateDestroyEnemies}'s
     * scaling range/damage, when the feature is enabled.
     */
    @Inject(method = "updateDestroyTarget", at = @At("HEAD"), cancellable = true)
    private static void insanesurvivaloverhaul$onUpdateDestroyTarget(Level level, BlockPos blockPos, BlockState state, List<BlockPos> blocks, ConduitBlockEntity conduit, CallbackInfo ci) {
        if (Conduit.conduitUpdateDestroyEnemies(level, blockPos, blocks))
            ci.cancel();
    }
}
