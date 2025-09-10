package insane96mcp.iguanatweaksreborn.module.combat.fletching.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class ISOArrow extends Arrow {
    protected double gravityForce = 0.05f;
    protected double friction = 0.99f;
    protected float velocityMultiplier = 1f;
    public ISOArrow(EntityType<? extends Arrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void tick() {
        super.tick();
        boolean isNoPhysics = this.isNoPhysics();
        if (!this.inGround || isNoPhysics) {
            if (!this.isNoGravity() && !isNoPhysics) {
                //Nullify vanilla gravity & add Custom Gravity
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.05d, 0).add(0, -this.gravityForce, 0));
            }
            //Nullify vanilla friction & set custom friction
            this.setDeltaMovement(this.getDeltaMovement().scale(1f / 0.99f).scale(this.friction));
        }
    }

    @Override
    public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
        super.shoot(pX, pY, pZ, pVelocity * this.velocityMultiplier, pInaccuracy);
    }

    @Override
    protected float getWaterInertia() {
        return super.getWaterInertia();
    }
}
