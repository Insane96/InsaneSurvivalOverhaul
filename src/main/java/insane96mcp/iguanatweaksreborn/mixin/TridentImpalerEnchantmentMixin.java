package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.item.enchantment.TridentImpalerEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TridentImpalerEnchantment.class)
public class TridentImpalerEnchantmentMixin {
    @ModifyExpressionValue(method = "getDamageBonus", at = @At(value = "CONSTANT", args = "floatValue=2.5"))
    public float iguanatweaksreborn$onDamageBonus(float original) {
        if (!Feature.isEnabled(EnchantmentsFeature.class)
                || !EnchantmentsFeature.replaceDamagingEnchantments)
            return original;
        return 1f;
    }
}
