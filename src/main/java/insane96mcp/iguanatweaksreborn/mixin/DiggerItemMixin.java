package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.iguanatweaksreborn.module.combat.MiscStats;
import insane96mcp.iguanatweaksreborn.module.items.misc.ItemDefinition;
import insane96mcp.iguanatweaksreborn.module.items.misc.ItemDefinitionsReloadListener;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DiggerItem.class)
public class DiggerItemMixin {
	@ModifyExpressionValue(method = "hurtEnemy", at = @At(value = "CONSTANT", args = "intValue=2"))
	public int onHurtEnemy(int hurtAmount, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        int amount = Feature.isEnabled(MiscStats.class) && MiscStats.oneDamageForToolAttacking ? 1 : hurtAmount;
        for (ItemDefinition itemDefinition : ItemDefinitionsReloadListener.DEFINITIONS)
            amount = itemDefinition.getDurabilityConsumed(amount, stack, attacker.getRandom());
		return amount;
	}
}
