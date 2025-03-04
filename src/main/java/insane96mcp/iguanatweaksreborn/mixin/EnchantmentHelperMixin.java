package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Redirect(method = "getEnchantmentCost", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getEnchantmentValue()I"))
    private static int onGetEnchantmentCost_getEnchantmentValue(ItemStack stack) {
        return EnchantmentsFeature.getEnchantmentValue(stack);
    }
    @Redirect(method = "selectEnchantment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getEnchantmentValue()I"))
    private static int onSelectEnchantment_getEnchantmentValue(ItemStack stack) {
        return EnchantmentsFeature.getEnchantmentValue(stack);
    }

    @Inject(method = "getMobLooting", at = @At(value = "RETURN"), cancellable = true)
    private static void onGetMobLooting(LivingEntity pEntity, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(EnchantmentsFeature.getLuckLevel(cir.getReturnValue(), EnchantmentHelper.getEnchantmentLevel(EnchantmentsFeature.LUCK.get(), pEntity)));
    }

    @Inject(method = "getFishingLuckBonus", at = @At(value = "RETURN"), cancellable = true)
    private static void onGetFishingLuckBonus(ItemStack pStack, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(EnchantmentsFeature.getLuckLevel(cir.getReturnValue(), pStack.getEnchantmentLevel(EnchantmentsFeature.LUCK.get())));
    }
}
