package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    @Invoker
    void invokeSetPierceLevel(byte pierceLevel);

    @Accessor("pickup")
    AbstractArrow.Pickup getPickup();

    @Invoker("getPickupItem")
    ItemStack invokeGetPickupItem();

    /**
     * Custom arrows are built through {@link AbstractArrow}'s bare (EntityType, Level) constructor, which never
     * runs the vanilla constructor that sets this field - so without this, every custom arrow's
     * {@code getWeaponItem()} stays null instead of the bow/crossbow that fired it, breaking anything that
     * inspects the killing weapon (e.g. RuneEnchanting's on-kill runes).
     */
    @Accessor("firedFromWeapon")
    void setFiredFromWeapon(@Nullable ItemStack stack);
}
