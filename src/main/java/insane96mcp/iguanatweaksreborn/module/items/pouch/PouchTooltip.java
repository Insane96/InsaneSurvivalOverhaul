package insane96mcp.iguanatweaksreborn.module.items.pouch;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record PouchTooltip(NonNullList<ItemStack> items) implements TooltipComponent {
}
