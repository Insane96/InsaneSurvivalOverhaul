package insane96mcp.insanesurvivaloverhaul.module.combat.fletching.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class ISOArrow extends Arrow {
    protected double gravity = 0.05;

    public ISOArrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected double getDefaultGravity() {
        return this.gravity;
    }
}
