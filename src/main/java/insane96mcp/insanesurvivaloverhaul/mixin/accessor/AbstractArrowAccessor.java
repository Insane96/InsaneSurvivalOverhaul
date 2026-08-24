package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    @Invoker
    void invokeSetPierceLevel(byte pierceLevel);

    @Accessor("pickup")
    AbstractArrow.Pickup getPickup();

    @Invoker("getPickupItem")
    ItemStack invokeGetPickupItem();
}
