package insane96mcp.insanesurvivaloverhaul.mixin.module.movement.minecarts;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanesurvivaloverhaul.module.movement.minecarts.ISOPoweredRail;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IAbstractMinecartExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin_Minecarts extends Entity implements IAbstractMinecartExtension {

    public AbstractMinecartMixin_Minecarts(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @ModifyExpressionValue(method = "moveAlongTrack", at = @At(value = "CONSTANT", args = "doubleValue=0.06"))
    private double insanesurvivaloverhaul$preventAcceleration(double acceleration, BlockPos pos, BlockState state) {
        BaseRailBlock baserailblock = (BaseRailBlock) state.getBlock();
        float railMaxSpeed = baserailblock.getRailMaxSpeed(state, this.level(), pos, (AbstractMinecart) (Object) this);
        if (this.getDeltaMovement().horizontalDistance() >= railMaxSpeed)
            return 0f;
        else if (baserailblock instanceof ISOPoweredRail ISOPoweredRail)
            return ISOPoweredRail.getRailAcceleration(state, this.level(), pos, (AbstractMinecart) (Object) this);
        return acceleration;
    }

    @WrapOperation(method = "getMaxSpeedWithRail", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BaseRailBlock;getRailMaxSpeed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/vehicle/AbstractMinecart;)F"), remap = false)
    public float insanesurvivaloverhaul$railMaxSpeed(BaseRailBlock instance, BlockState blockState, Level level, BlockPos blockPos, AbstractMinecart abstractMinecart, Operation<Float> original) {
        BlockPos pos = this.getCurrentRailPosition();
        BlockState state = this.level().getBlockState(pos);
        if (state.is(Blocks.RAIL))
            return 0.7f;
        return original.call(instance, blockState, level, blockPos, abstractMinecart);
    }
}
