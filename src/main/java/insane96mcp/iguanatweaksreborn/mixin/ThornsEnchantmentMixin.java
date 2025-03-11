package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ThornsEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ThornsEnchantment.class)
public abstract class ThornsEnchantmentMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"), method = "doPostHurt")
    private void onPostHurtThorns(ItemStack stack, int amount, LivingEntity entity, Consumer<LivingEntity> pOnBroken) {
        //If thorns overhaul is enabled don't call hurtAndBreak
        if (EnchantmentsFeature.isThornsOverhaul())
            return;
        stack.hurtAndBreak(amount, entity, pOnBroken);
    }

    @Inject(method = "shouldHit", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private static void onShouldHit(int pLevel, RandomSource pRandom, CallbackInfoReturnable<Boolean> cir) {
        if (!EnchantmentsFeature.isThornsOverhaul())
            return;

        cir.setReturnValue(true);
    }

    @Inject(method = "getDamage", at = @At("RETURN"), cancellable = true)
    private static void onGetDamage(int lvl, RandomSource random, CallbackInfoReturnable<Integer> cir) {
        if (!EnchantmentsFeature.isThornsOverhaul())
            return;

        cir.setReturnValue(lvl);
    }
}
