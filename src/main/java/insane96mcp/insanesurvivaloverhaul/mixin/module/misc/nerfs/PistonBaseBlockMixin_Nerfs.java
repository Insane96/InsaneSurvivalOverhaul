package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.nerfs;

import insane96mcp.insanesurvivaloverhaul.module.misc.Nerfs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin_Nerfs {
    @Unique
    private BlockPos insanesurvivaloverhaul$oldPos;

    @Unique
    private BlockState insanesurvivaloverhaul$newState;

    @Unique
    private Map<BlockPos, BlockState> insanesurvivaloverhaul$storedMap;

    /**
     * Captures the source block position before it is moved by a piston, so it can be
     * referenced later to prevent ghost-fluid exploits during block movement.
     */
    @ModifyVariable(method = "moveBlocks", at = @At(value = "STORE", ordinal = 0), index = 15, ordinal = 2, slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addDestroyBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"), to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/MovingPistonBlock;newMovingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;ZZ)Lnet/minecraft/world/level/block/entity/BlockEntity;")))
    private BlockPos insanesurvivaloverhaul$storeOldPos(BlockPos pos) {
        insanesurvivaloverhaul$oldPos = pos;
        return pos;
    }

    /**
     * Captures the block position-to-state map built during piston movement so it can be
     * updated after fluid blocks change state when placed at their new position.
     */
    @ModifyVariable(method = "moveBlocks", at = @At(value = "STORE", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;resolve()Z"), to = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;")))
    private Map<BlockPos, BlockState> insanesurvivaloverhaul$storeMap(Map<BlockPos, BlockState> map) {
        insanesurvivaloverhaul$storedMap = map;
        return map;
    }

    /**
     * After a block is set at its new position, re-reads the actual world state (which may
     * differ for fluid blocks) and updates the stored map to prevent ghost-fluid exploits.
     */
    @Inject(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", ordinal = 2, shift = At.Shift.AFTER))
    private void insanesurvivaloverhaul$modifyBlockstate(Level worldIn, BlockPos posIn, Direction pistonFacing, boolean extending, CallbackInfoReturnable<Boolean> cir) {
        if (Nerfs.isPistonPhysicsExploitEnabled()) {
            insanesurvivaloverhaul$newState = worldIn.getBlockState(insanesurvivaloverhaul$oldPos);
            insanesurvivaloverhaul$storedMap.replace(insanesurvivaloverhaul$oldPos, insanesurvivaloverhaul$newState);
        }
    }

    /**
     * Provides the corrected (post-placement) block state to the moving piston block entity
     * so it carries the real fluid state rather than the stale pre-move state.
     */
    @ModifyArg(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/MovingPistonBlock;newMovingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;ZZ)Lnet/minecraft/world/level/block/entity/BlockEntity;", ordinal = 0), index = 2)
    private BlockState insanesurvivaloverhaul$modifyMovingBlockEntityState(BlockState state) {
        return Nerfs.isPistonPhysicsExploitEnabled() ? insanesurvivaloverhaul$newState : state;
    }

    /**
     * Sets the source position to air after the moving piston block entity is placed,
     * preventing later physics updates from seeing a stale block and causing fluid exploits.
     */
    @Inject(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void insanesurvivaloverhaul$setOldPosToAir(Level worldIn, BlockPos pos, Direction directionIn, boolean extending, CallbackInfoReturnable<Boolean> cir) {
        if (Nerfs.isPistonPhysicsExploitEnabled())
            worldIn.setBlock(insanesurvivaloverhaul$oldPos, Blocks.AIR.defaultBlockState(), 2 | 4 | 16 | 1024); // paper impl comment: set air to prevent later physics updates from seeing this block
    }
}
