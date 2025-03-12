package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.combat.bows.Bows;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @ModifyExpressionValue(method = "getShootingPower", at = @At(value = "CONSTANT", args = "floatValue=3.15"))
    private static float powerBonusFlat(float original) {
        return Bows.getCrossbowVelocity();
    }

    @Inject(method = "getArrow", at = @At(value = "RETURN"))
    private static void iguanatweaksreborn$onSetPiercing(Level pLevel, LivingEntity pLivingEntity, ItemStack pCrossbowStack, ItemStack pAmmoStack, CallbackInfoReturnable<AbstractArrow> cir, @Local AbstractArrow abstractArrow) {
        Bows.piercingCrossbows(abstractArrow);
    }

    @ModifyExpressionValue(method = "use", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
    private float iguanatweaksreborn$inaccuracy(float original) {
        return Bows.getCrossbowInaccuracy(original);
    }

    @Definition(id = "k", local = @Local(type = int.class, ordinal = 2))
    @Expression("k > 0")
    @WrapOperation(method = "tryLoadProjectiles", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private static boolean iguanatweaksreborn$effectiveMultishot(int left, int right, Operation<Boolean> original) {
        if (!Feature.isEnabled(EnchantmentsFeature.class)
                || !EnchantmentsFeature.actualMultishot)
            return original.call(left, right);
        return false;
    }

    @Definition(id = "getProjectile", method = "Lnet/minecraft/world/entity/player/Player;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;")
    @Definition(id = "isEmpty", method = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z")
    @Definition(id = "pPlayer", local = @Local(type = Player.class, argsOnly = true))
    @Expression("pPlayer.getProjectile(?).isEmpty()")
    @WrapOperation(method = "use", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean iguanatweaksreborn$effectiveMultishot(ItemStack instance, Operation<Boolean> original, @Local(argsOnly = true) Player player, @Local(argsOnly = true) InteractionHand hand) {
        Boolean originalR = original.call(instance);
        if (!Feature.isEnabled(EnchantmentsFeature.class)
                || !EnchantmentsFeature.actualMultishot)
            return originalR;
        if (originalR)
            return true;
        ItemStack handItem = player.getItemInHand(hand);
        int i = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.MULTISHOT, handItem);
        if (i <= 0)
            return false;
        if (instance.getCount() < 3) {
            player.displayClientMessage(Component.translatable("multishot.not_enough_arrows", Component.translatable(instance.getItem().getDescriptionId(instance)).withStyle(ChatFormatting.ITALIC)), true);
            return true;
        }
        return false;
    }

    @Definition(id = "pProjectileAngle", local = @Local(type = float.class, ordinal = 3, argsOnly = true))
    @Expression("pProjectileAngle != 0.0")
    @WrapOperation(method = "shootProjectile", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean iguanatweaksreborn$onCreativePickupSet(float left, float right, Operation<Boolean> original) {
        Boolean originalR = original.call(left, right);
        if (!Feature.isEnabled(EnchantmentsFeature.class)
                || !EnchantmentsFeature.actualMultishot)
            return originalR;
        return false;
    }
}
