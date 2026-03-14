package insane96mcp.insanesurvivaloverhaul.mixin.module.combat.misccombat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.combat.MiscCombat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DiggerItem.class)
public class DiggerItemMixin_MiscCombat {

    /**
     * Reduces the tool durability consumed on enemy hit from 2 to 1 when enabled,
     * making tools last twice as long when used as weapons.
     * @see MiscCombat#oneDamageForToolAttacking
     */
    @ModifyExpressionValue(method = "postHurtEnemy", at = @At(value = "CONSTANT", args = "intValue=2"))
    public int insanesurvivaloverhaul$toolAttackDurability(int original, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return Feature.isEnabled(MiscCombat.class) && MiscCombat.oneDamageForToolAttacking ? 1 : original;
    }
}
