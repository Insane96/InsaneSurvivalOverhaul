package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.combat.MiscStats;
import insane96mcp.iguanatweaksreborn.module.mining.Gold;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Tier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DiggerItem.class)
public class DiggerItemMixin {
	@Redirect(method = "isCorrectToolForDrops(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/DiggerItem;getTier()Lnet/minecraft/world/item/Tier;"))
	private Tier getTier(DiggerItem item) {
		return Gold.getEffectiveTier(item.getTier());
	}

	@ModifyConstant(method = "hurtEnemy", constant = @Constant(intValue = 2, ordinal = 0))
	public int onHurtEnemy(int hurtAmount) {
		return Feature.isEnabled(MiscStats.class) && MiscStats.oneDamageForToolAttacking ? 1 : hurtAmount;
	}
}
