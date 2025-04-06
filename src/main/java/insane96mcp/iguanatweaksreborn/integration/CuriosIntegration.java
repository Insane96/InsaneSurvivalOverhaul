package insane96mcp.iguanatweaksreborn.integration;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.List;
import java.util.Map;

public class CuriosIntegration {
    public static void onDeath(List<ItemStack> items, Player player) {
        LazyOptional<ICuriosItemHandler> maybeCuriosInventory = CuriosApi.getCuriosInventory(player);
        maybeCuriosInventory.ifPresent(curiosInventory -> {
            Map<String, ICurioStacksHandler> curios = curiosInventory.getCurios();
            curios.forEach((id, slotInventory) -> {
                saveAndRemoveItem(items, slotInventory.getStacks());
                saveAndRemoveItem(items, slotInventory.getCosmeticStacks());
            });
        });
    }

    public static void saveAndRemoveItem(List<ItemStack> items, IDynamicStackHandler dynamicStackHandler) {
        for (int i = 0; i < dynamicStackHandler.getSlots(); i++) {
            ItemStack stack = dynamicStackHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                items.add(stack);
                dynamicStackHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }
}
