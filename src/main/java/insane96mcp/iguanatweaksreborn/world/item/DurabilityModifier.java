package insane96mcp.iguanatweaksreborn.world.item;

import net.minecraft.world.item.ItemStack;

public interface DurabilityModifier {
    float getDurabilityMultiplier(ItemStack stack);
}
