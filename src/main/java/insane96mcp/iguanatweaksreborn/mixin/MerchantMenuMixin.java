package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.iguanatweaksreborn.world.inventory.UnboundSlot;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MerchantMenu.class)
public class MerchantMenuMixin {
    @Definition(id = "Slot", type = Slot.class)
    @Definition(id = "tradeContainer", field = "Lnet/minecraft/world/inventory/MerchantMenu;tradeContainer:Lnet/minecraft/world/inventory/MerchantContainer;")
    @Expression("new Slot(?.tradeContainer, ?, ?, ?)")
    @WrapOperation(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/trading/Merchant;)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    public Slot iguanatweaksreborn$replaceMerchantSlotWithUnbound(Container pContainer, int pSlot, int pX, int pY, Operation<Slot> original) {
        return new UnboundSlot(pContainer, pSlot, pX, pY);
    }

    @ModifyExpressionValue(method = "moveFromInventoryToPaymentSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
    public int iguanatweaksreborn$preventClampStackSize(int original) {
        return 64;
    }
}
