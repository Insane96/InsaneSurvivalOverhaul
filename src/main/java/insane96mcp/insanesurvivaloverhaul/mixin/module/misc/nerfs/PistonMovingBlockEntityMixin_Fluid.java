package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.nerfs;

import insane96mcp.insanesurvivaloverhaul.module.misc.Nerfs;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PistonMovingBlockEntity.class)
public class PistonMovingBlockEntityMixin_Fluid {
    /**
     * Adds the notify flag (2) to the block-set call during piston retraction, ensuring
     * neighbors are updated and preventing ghost fluids from persisting after the piston retracts.
     */
    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", ordinal = 0), index = 2)
    private static int insanesurvivaloverhaul$modifySetBlockFlags(int flag) {
        return Nerfs.isPistonPhysicsExploitEnabled() ? (84 | 2) : 84; // paper impl comment: Paper - force notify (flag 2), it's possible the set type by the piston block (which doesn't notify) set this block to air
    }
}
