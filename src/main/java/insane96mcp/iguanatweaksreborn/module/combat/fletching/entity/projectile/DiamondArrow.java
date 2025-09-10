package insane96mcp.iguanatweaksreborn.module.combat.fletching.entity.projectile;

import insane96mcp.iguanatweaksreborn.module.combat.fletching.Fletching;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DiamondArrow extends ISOArrow {
    public DiamondArrow(EntityType<? extends Arrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.gravityForce = 0.08f;
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Fletching.DIAMOND_ARROW_ITEM.get());
    }
}
