package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    @Invoker
    void invokeSetPierceLevel(byte pierceLevel);
}
