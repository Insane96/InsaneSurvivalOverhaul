package insane96mcp.insanesurvivaloverhaul.module.combat.fletching.entity;

import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.FletchingFeature;
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
    public ExplosiveArrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(FletchingFeature.EXPLOSIVE_ARROW_ITEM.get());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.explode();
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entityHit = result.getEntity();
        Entity owner = this.getOwner();
        if (owner instanceof LivingEntity livingOwner)
            livingOwner.setLastHurtMob(entityHit);

        // Endermen teleport away from most projectiles instead of taking the hit; let that happen instead of
        // exploding on top of them, mirroring how vanilla arrows bounce off without triggering their effect.
        if (entityHit.getType() != EntityType.ENDERMAN) {
            this.explode();
            this.discard();
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
            this.setYRot(this.getYRot() + 180.0F);
            this.yRotO += 180.0F;
            if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
                if (this.pickup == AbstractArrow.Pickup.ALLOWED)
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                this.discard();
            }
        }
    }

    private void explode() {
        if (!this.level().isClientSide) {
            float power = Math.min(1f + (float) this.getDeltaMovement().length() * 0.5f, 6f);
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), power, Level.ExplosionInteraction.BLOCK);
        }
    }
}
