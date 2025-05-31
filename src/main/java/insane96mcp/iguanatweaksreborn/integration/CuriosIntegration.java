package insane96mcp.iguanatweaksreborn.integration;

import insane96mcp.iguanatweaksreborn.module.sleeprespawn.death.GraveBlockEntity;
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
    public static void onDeath(List<GraveBlockEntity.SlottedStack> items, Player player) {
        LazyOptional<ICuriosItemHandler> maybeCuriosInventory = CuriosApi.getCuriosInventory(player);
        maybeCuriosInventory.ifPresent(curiosInventory -> {
            Map<String, ICurioStacksHandler> curios = curiosInventory.getCurios();
            curios.forEach((id, slotInventory) -> {
                saveAndRemoveItem(items, slotInventory.getStacks());
                saveAndRemoveItem(items, slotInventory.getCosmeticStacks());
            });
        });
    }

    public static void saveAndRemoveItem(List<GraveBlockEntity.SlottedStack> items, IDynamicStackHandler dynamicStackHandler) {
        for (int i = 0; i < dynamicStackHandler.getSlots(); i++) {
            ItemStack stack = dynamicStackHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                items.add(new GraveBlockEntity.SlottedStack(stack));
                dynamicStackHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }
}
