package insane96mcp.iguanatweaksreborn.module.movement.minecarts;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ISOPoweredRail extends PoweredRailBlock {
    final float baseSpeed;
    final float acceleration;
    public ISOPoweredRail(Properties properties, float baseSpeed, float acceleration) {
        super(properties, true);
        this.baseSpeed = baseSpeed;
        this.acceleration = acceleration;
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        if (cart instanceof MinecartFurnace)
            return cart.isInWater() ? this.baseSpeed * 0.375f : this.baseSpeed * 0.5f;
        else return cart.isInWater() ? this.baseSpeed * 0.5f : this.baseSpeed;
    }

    public float getRailAcceleration(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        return this.acceleration;
    }

}
