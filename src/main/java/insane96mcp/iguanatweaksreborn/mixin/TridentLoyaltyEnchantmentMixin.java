package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import net.minecraft.world.item.enchantment.TridentLoyaltyEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TridentLoyaltyEnchantment.class)
public class TridentLoyaltyEnchantmentMixin {
    @ModifyReturnValue(method = "getMinCost", at = @At("RETURN"))
    public int iguanatweaksreborn$getMinCost(int cost) {
        if (!EnchantmentsFeature.isLoyaltyMiniReworkEnabled())
            return cost;
        return 19;
    }

    @ModifyReturnValue(method = "getMaxCost", at = @At("RETURN"))
    public int iguanatweaksreborn$getMaxCost(int cost) {
        if (!EnchantmentsFeature.isLoyaltyMiniReworkEnabled())
            return cost;
        return 50;
    }

    @ModifyReturnValue(method = "getMaxLevel", at = @At("RETURN"))
    public int iguanatweaksreborn$getMaxLevel(int maxLvl) {
        if (!EnchantmentsFeature.isLoyaltyMiniReworkEnabled())
            return maxLvl;
        return 1;
    }
}
