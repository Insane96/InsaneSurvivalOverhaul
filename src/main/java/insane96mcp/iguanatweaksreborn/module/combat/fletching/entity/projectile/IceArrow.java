package insane96mcp.iguanatweaksreborn.module.combat.fletching.entity.projectile;

import insane96mcp.iguanatweaksreborn.module.combat.fletching.Fletching;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IceArrow extends ISOArrow {
    public IceArrow(EntityType<? extends Arrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.gravityForce = 0.08f;
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Fletching.ICE_ARROW_ITEM.get());
    }

    @Override
    protected void doPostHurtEffects(LivingEntity pLiving) {
        super.doPostHurtEffects(pLiving);
        if (pLiving.getTicksFrozen() < pLiving.getTicksRequiredToFreeze() * 3)
            pLiving.setTicksFrozen(pLiving.getTicksRequiredToFreeze() * 3);
    }
}
