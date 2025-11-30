package insane96mcp.iguanatweaksreborn.mixin;

import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FallingBlockEntity.class)
public interface FallingBlockEntityAccessor {
    @Invoker("<init>")
    static FallingBlockEntity ctor(Level pLevel, double pX, double pY, double pZ, BlockState pState) {
        return null;
    }
}
