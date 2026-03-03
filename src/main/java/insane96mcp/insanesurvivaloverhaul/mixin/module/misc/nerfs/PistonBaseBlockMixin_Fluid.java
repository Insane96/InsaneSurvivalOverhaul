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
public class PistonBaseBlockMixin_Fluid {
    @Unique
    private BlockPos iguanaTweaksReborn$oldPos;

    @Unique
    private BlockState iguanaTweaksReborn$newState;

    @Unique
    private Map<BlockPos, BlockState> iguanaTweaksReborn$storedMap;

    @ModifyVariable(method = "moveBlocks", at = @At(value = "STORE", ordinal = 0), index = 15, ordinal = 2, slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addDestroyBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"), to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/MovingPistonBlock;newMovingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;ZZ)Lnet/minecraft/world/level/block/entity/BlockEntity;")))
    private BlockPos insanesurvivaloverhaul$storeOldPos(BlockPos pos) {
        iguanaTweaksReborn$oldPos = pos;
        return pos;
    }

    @ModifyVariable(method = "moveBlocks", at = @At(value = "STORE", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;resolve()Z"), to = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;")))
    private Map<BlockPos, BlockState> insanesurvivaloverhaul$storeMap(Map<BlockPos, BlockState> map) {
        iguanaTweaksReborn$storedMap = map;
        return map;
    }

    @Inject(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", ordinal = 2, shift = At.Shift.AFTER))
    private void insanesurvivaloverhaul$modifyBlockstate(Level worldIn, BlockPos posIn, Direction pistonFacing, boolean extending, CallbackInfoReturnable<Boolean> cir) {
        if (Nerfs.isPistonPhysicsExploitEnabled()) {
            iguanaTweaksReborn$newState = worldIn.getBlockState(iguanaTweaksReborn$oldPos);
            iguanaTweaksReborn$storedMap.replace(iguanaTweaksReborn$oldPos, iguanaTweaksReborn$newState);
        }
    }

    @ModifyArg(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/MovingPistonBlock;newMovingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;ZZ)Lnet/minecraft/world/level/block/entity/BlockEntity;", ordinal = 0), index = 2)
    private BlockState insanesurvivaloverhaul$modifyMovingBlockEntityState(BlockState state) {
        return Nerfs.isPistonPhysicsExploitEnabled() ? iguanaTweaksReborn$newState : state;
    }

    @Inject(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void insanesurvivaloverhaul$setOldPosToAir(Level worldIn, BlockPos pos, Direction directionIn, boolean extending, CallbackInfoReturnable<Boolean> cir) {
        if (Nerfs.isPistonPhysicsExploitEnabled())
            worldIn.setBlock(iguanaTweaksReborn$oldPos, Blocks.AIR.defaultBlockState(), 2 | 4 | 16 | 1024); // paper impl comment: set air to prevent later physics updates from seeing this block
    }
}
