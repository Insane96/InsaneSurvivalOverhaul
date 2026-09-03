package insane96mcp.insanesurvivaloverhaul.module.combat.fletching.entity;

import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.FletchingFeature;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IceArrow extends ISOArrow {
    public IceArrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
        this.gravity = 0.08;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(FletchingFeature.ICE_ARROW_ITEM.get());
    }

    @Override
    protected void doPostHurtEffects(LivingEntity living) {
        super.doPostHurtEffects(living);
        if (living.getTicksFrozen() < living.getTicksRequiredToFreeze() * 2)
            living.setTicksFrozen(living.getTicksRequiredToFreeze() * 2);
    }
}
