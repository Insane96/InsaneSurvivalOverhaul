package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(PortalShape.class)
public interface PortalShapeAccessor {
    @Accessor
    @Nullable
    BlockPos getBottomLeft();

    @Accessor
    Direction getRightDir();

    @Accessor
    int getHeight();

    @Accessor
    int getWidth();
}
