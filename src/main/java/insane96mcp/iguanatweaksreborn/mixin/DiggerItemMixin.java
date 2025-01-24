package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.combat.MiscStats;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.item.DiggerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DiggerItem.class)
public class DiggerItemMixin {
	@ModifyConstant(method = "hurtEnemy", constant = @Constant(intValue = 2, ordinal = 0))
	public int onHurtEnemy(int hurtAmount) {
		return Feature.isEnabled(MiscStats.class) && MiscStats.oneDamageForToolAttacking ? 1 : hurtAmount;
	}
}
