package insane96mcp.insanesurvivaloverhaul.module.combat.fletching.entity;

import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.FletchingFeature;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DiamondArrow extends ISOArrow {
    public DiamondArrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
        this.gravity = 0.08;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(FletchingFeature.DIAMOND_ARROW_ITEM.get());
    }
}
