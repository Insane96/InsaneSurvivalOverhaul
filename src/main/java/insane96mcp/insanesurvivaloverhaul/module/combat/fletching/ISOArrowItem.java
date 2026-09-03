package insane96mcp.insanesurvivaloverhaul.module.combat.fletching;

import insane96mcp.insanesurvivaloverhaul.mixin.accessor.AbstractArrowAccessor;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * An {@link ArrowItem} that fires a custom {@link Arrow} subclass instead of the vanilla one, both when shot
 * from a bow/crossbow ({@link #createArrow}) and when fired from a dispenser ({@link #asProjectile}).
 */
public class ISOArrowItem extends ArrowItem {
    private final Supplier<EntityType<? extends Arrow>> arrowType;
    private final float baseDamage;

    public ISOArrowItem(Supplier<EntityType<? extends Arrow>> arrowType, float baseDamage, Properties properties) {
        super(properties);
        this.arrowType = arrowType;
        this.baseDamage = baseDamage;
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        Arrow arrow = this.arrowType.get().create(level);
        if (arrow == null)
            return super.createArrow(level, ammo, shooter, weapon);
        arrow.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
        arrow.setOwner(shooter);
        if (shooter instanceof Player)
            arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        arrow.setBaseDamage(this.baseDamage);
        if (weapon != null)
            ((AbstractArrowAccessor) arrow).setFiredFromWeapon(weapon.copy());
        return arrow;
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        Arrow arrow = this.arrowType.get().create(level);
        if (arrow == null)
            return super.asProjectile(level, pos, stack, direction);
        arrow.setPos(pos.x(), pos.y(), pos.z());
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        arrow.setBaseDamage(this.baseDamage);
        return arrow;
    }
}
