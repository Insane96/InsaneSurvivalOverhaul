package insane96mcp.insanesurvivaloverhaul.module.combat.fletching.entity;

import insane96mcp.insanesurvivaloverhaul.module.combat.PiercingDamage;
import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.FletchingFeature;
import insane96mcp.insanesurvivaloverhaul.util.MCUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class QuartzArrow extends ISOArrow {
    /**
     * Piercing damage converted from the last arrow hit, set by {@link FletchingFeature#onIncomingDamage} and consumed by {@link #doPostHurtEffects}.
     */
    private float pendingPiercingDamage = 0f;

    public QuartzArrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(FletchingFeature.QUARTZ_ARROW_ITEM.get());
    }

    public void setPendingPiercingDamage(float pendingPiercingDamage) {
        this.pendingPiercingDamage = pendingPiercingDamage;
    }

    /**
     * Deals the part of the arrow's damage converted into piercing damage (see {@link FletchingFeature#quartzArrowPiercingRatio})
     * as a second hit that ignores the target's invulnerability frames.
     */
    @Override
    protected void doPostHurtEffects(LivingEntity living) {
        super.doPostHurtEffects(living);
        float damage = this.pendingPiercingDamage;
        this.pendingPiercingDamage = 0f;
        if (damage <= 0f)
            return;
        Entity owner = this.getOwner();
        DamageSource damageSource;
        if (owner == null) {
            damageSource = this.damageSources().source(PiercingDamage.PIERCING_MOB_ATTACK, this);
        } else {
            damageSource = this.damageSources().source(owner instanceof Player ? PiercingDamage.PIERCING_PLAYER_ATTACK : PiercingDamage.PIERCING_MOB_ATTACK, owner);
            if (owner instanceof LivingEntity livingOwner)
                livingOwner.setLastHurtMob(living);
        }
        MCUtils.attackEntityIgnoreInvFrames(damageSource, damage, living, null, true);
    }
}
