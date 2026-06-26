package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.iguanatweaksreborn.module.movement.minecarts.ISOPoweredRail;
import insane96mcp.iguanatweaksreborn.module.movement.minecarts.Minecarts;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeAbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends Entity implements IForgeAbstractMinecart {

    public AbstractMinecartMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @ModifyExpressionValue(method = "hurt", at = @At(value = "CONSTANT", args = "floatValue=40.0"))
    public float damageToBreak(float damageToBreak) {
        return 20f;
    }

    @ModifyExpressionValue(method = "moveAlongTrack", at = @At(value = "CONSTANT", args = "doubleValue=0.06"))
    private double preventAcceleration(double acceleration, BlockPos pos, BlockState state) {
        if (!Feature.isEnabled(Minecarts.class))
            return acceleration;
        BaseRailBlock baserailblock = (BaseRailBlock) state.getBlock();
        float railMaxSpeed = baserailblock.getRailMaxSpeed(state, this.level(), pos, (AbstractMinecart) (Object) this);
        if (this.getDeltaMovement().horizontalDistance() >= railMaxSpeed)
            return 0f;
        else if (baserailblock instanceof ISOPoweredRail ISOPoweredRail)
            return ISOPoweredRail.getRailAcceleration(state, this.level(), pos, (AbstractMinecart) (Object) this);
        return acceleration;
    }

    @WrapOperation(method = "getMaxSpeedWithRail", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BaseRailBlock;getRailMaxSpeed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/vehicle/AbstractMinecart;)F"), remap = false)
    public float railMaxSpeed(BaseRailBlock instance, BlockState blockState, Level level, BlockPos blockPos, AbstractMinecart abstractMinecart, Operation<Float> original) {
        if (!Feature.isEnabled(Minecarts.class))
            return original.call(instance, blockState, level, blockPos, abstractMinecart);
        BlockPos pos = this.getCurrentRailPosition();
        BlockState state = this.level().getBlockState(pos);
        if (state.is(Blocks.RAIL))
            return 0.7f;
        return original.call(instance, blockState, level, blockPos, abstractMinecart);
    }

    /*@ModifyReturnValue(method = "getMaxSpeed", at = @At("RETURN"))
    public double railMaxSpeed(double original) {
        if (Minecarts.speedUpMinecartsUnderwater())
            return 8d / 20d;
        return original;
    }*/
}
