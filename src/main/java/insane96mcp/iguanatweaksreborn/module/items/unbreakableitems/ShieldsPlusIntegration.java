package insane96mcp.iguanatweaksreborn.module.items.unbreakableitems;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.shieldsplus.event.BlockWithCrouchEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShieldsPlusIntegration {
    public static void onBlockWithCrouch(BlockWithCrouchEvent event) {
        if (!Feature.isEnabled(UnbreakableItems.class))
            return;

        ItemStack stack = event.getStack();
        if (stack.getMaxDamage() == 0)
            return;
        if (UnbreakableItems.isBroken(stack)) {
            event.setCanceled(true);
            if (event.getEntity() instanceof Player player)
                player.displayClientMessage(Component.translatable(UnbreakableItems.BROKEN_ITEM_LANG), true);
        }
    }
}
