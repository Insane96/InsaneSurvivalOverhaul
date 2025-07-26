package insane96mcp.iguanatweaksreborn.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Same as slot, but the stack limit is ignored (except for items that stack to 1)
 */
public class UnboundSlot extends Slot {
    public UnboundSlot(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        if (stack.getMaxStackSize() == 1)
            return 1;
        return this.getMaxStackSize();
    }
}
