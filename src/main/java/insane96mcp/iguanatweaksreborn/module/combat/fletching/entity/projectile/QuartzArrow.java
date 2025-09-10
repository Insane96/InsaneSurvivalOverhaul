package insane96mcp.iguanatweaksreborn.module.combat.fletching.entity.projectile;

import insane96mcp.iguanatweaksreborn.module.combat.PiercingDamage;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.Fletching;
import insane96mcp.iguanatweaksreborn.utils.MCUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class QuartzArrow extends ISOArrow {
    public QuartzArrow(EntityType<? extends Arrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Fletching.QUARTZ_ARROW_ITEM.get());
    }

    @Override
    protected void doPostHurtEffects(LivingEntity entity) {
        super.doPostHurtEffects(entity);
        Entity entity1 = this.getOwner();
        DamageSource damageSource;
        if (entity1 == null) {
            damageSource = this.damageSources().source(PiercingDamage.PIERCING_MOB_ATTACK, this);
        } else {
            damageSource = this.damageSources().source(PiercingDamage.PIERCING_MOB_ATTACK, entity1);
            if (entity1 instanceof LivingEntity) {
                ((LivingEntity)entity1).setLastHurtMob(entity);
            }
        }
        float damage = (float) ((0.5f + this.getBaseDamage()) * this.getDeltaMovement().length());

        MCUtils.attackEntityIgnoreInvFrames(damageSource, damage, entity, null, true);
    }

    @Override
    public double getBaseDamage() {
        return super.getBaseDamage();
    }
}
