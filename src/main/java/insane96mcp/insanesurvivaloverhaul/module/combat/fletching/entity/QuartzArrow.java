package insane96mcp.insanesurvivaloverhaul.module.combat.fletching.entity;

import insane96mcp.insanesurvivaloverhaul.module.combat.PiercingDamage;
import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.FletchingFeature;
import insane96mcp.insanesurvivaloverhaul.util.MCUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class QuartzArrow extends ISOArrow {
    public QuartzArrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(FletchingFeature.QUARTZ_ARROW_ITEM.get());
    }

    /**
     * On top of vanilla's own damage, deals a second hit that ignores the target's invulnerability frames,
     * letting the arrow's speed contribute extra damage beyond what a single hit could otherwise land.
     */
    @Override
    protected void doPostHurtEffects(LivingEntity living) {
        super.doPostHurtEffects(living);
        Entity owner = this.getOwner();
        DamageSource damageSource;
        if (owner == null) {
            damageSource = this.damageSources().source(PiercingDamage.PIERCING_MOB_ATTACK, this);
        } else {
            damageSource = this.damageSources().source(PiercingDamage.PIERCING_MOB_ATTACK, owner);
            if (owner instanceof LivingEntity livingOwner)
                livingOwner.setLastHurtMob(living);
        }
        float damage = (float) ((0.5f + this.getBaseDamage()) * this.getDeltaMovement().length());
        MCUtils.attackEntityIgnoreInvFrames(damageSource, damage, living, null, true);
    }
}
