package insane96mcp.iguanatweaksreborn.module.combat.fletching.dispenser;

import insane96mcp.iguanatweaksreborn.module.combat.fletching.item.ISOArrowItem;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ISOArrowDispenseBehaviour extends AbstractProjectileDispenseBehavior {
	@Override
	protected Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack) {
		return ((ISOArrowItem)pStack.getItem()).createDispenserArrow(pLevel, pPosition, pStack);
	}
}
