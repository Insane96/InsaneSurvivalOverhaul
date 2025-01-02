package insane96mcp.iguanatweaksreborn.module.items.flintexpansion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GroundFlintBlock extends GroundRockBlock {
    protected static final VoxelShape SHAPE = Block.box(4.5D, 0.0D, 4.5D, 12.0D, 1.5D, 12.0D);

    public GroundFlintBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
        Vec3 vec3 = blockState.getOffset(blockGetter, pos);
        return SHAPE.move(vec3.x, vec3.y, vec3.z);
    }
}
