package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArrowItem.class)
public class ArrowItemMixin {
    @Inject(at = @At("RETURN"), method = "isInfinite", cancellable = true, remap = false)
    private void onIsInfinite(ItemStack stack, ItemStack bow, Player player, CallbackInfoReturnable<Boolean> cir) {
        //Change the return value only if the original method had returned true
        if (!cir.getReturnValue() || !EnchantmentsFeature.isInfinityOverhaulEnabled())
            return;
        int enchant = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow);
        RandomSource random = MCUtils.syncedRandom(player);
        if (random.nextInt(enchant + 1) == 0)
            cir.setReturnValue(false);
    }
}
