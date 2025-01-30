package insane96mcp.iguanatweaksreborn.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

/**
 * Called to determine the max damage (durability) of a stack
 */
public class StackMaxDamageEvent extends Event {
    ItemStack stack;
    int originalMaxDamage;
    int newMaxDamage;
    public StackMaxDamageEvent(ItemStack stack, int originalMaxDamage)
    {
        this.stack = stack;
        this.originalMaxDamage = originalMaxDamage;
        this.newMaxDamage = originalMaxDamage;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public int getOriginalMaxDamage() {
        return this.originalMaxDamage;
    }

    public int getNewMaxDamage() {
        return this.newMaxDamage;
    }

    public void setNewMaxDamage(int newMaxDamage) {
        this.newMaxDamage = newMaxDamage;
    }
}
