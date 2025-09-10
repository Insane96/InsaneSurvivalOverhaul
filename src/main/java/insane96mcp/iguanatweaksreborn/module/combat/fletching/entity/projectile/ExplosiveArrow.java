package insane96mcp.iguanatweaksreborn.module.combat.fletching.entity.projectile;

import insane96mcp.iguanatweaksreborn.module.combat.fletching.Fletching;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class ExplosiveArrow extends Arrow {
    public ExplosiveArrow(EntityType<? extends Arrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ExplosiveArrow(Level pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ);
    }

    public ExplosiveArrow(Level pLevel, LivingEntity pShooter) {
        super(pLevel, pShooter);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        float power = Math.min(1f + (float) this.getDeltaMovement().length() * 0.5f, 6f);
        if (!this.level().isClientSide)
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), power, Level.ExplosionInteraction.BLOCK);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity entityHit = pResult.getEntity();
        /*if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }

            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }

            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }

            this.piercingIgnoreEntityIds.add(entityHit.getId());
        }*/

        Entity owner = this.getOwner();
        DamageSource damagesource;
        if (owner == null) {
            damagesource = this.damageSources().arrow(this, this);
        }
        else {
            damagesource = this.damageSources().arrow(this, owner);
            if (owner instanceof LivingEntity) {
                ((LivingEntity)owner).setLastHurtMob(entityHit);
            }
        }

        boolean isEnderman = entityHit.getType() == EntityType.ENDERMAN;
        if (!isEnderman) {
            float power = Math.min(1f + (float) this.getDeltaMovement().length() * 0.5f, 6f);
            if (!this.level().isClientSide)
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), power, Level.ExplosionInteraction.BLOCK);
            this.discard();
        }
        else {
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
            this.setYRot(this.getYRot() + 180.0F);
            this.yRotO += 180.0F;
            if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
                if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.discard();
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Fletching.EXPLOSIVE_ARROW_ITEM.get());
    }
}
