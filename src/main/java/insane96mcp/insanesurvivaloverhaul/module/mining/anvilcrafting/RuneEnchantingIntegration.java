package insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting;

import insane96mcp.runeenchanting.RuneHelper;
import net.minecraft.world.item.ItemStack;

public class RuneEnchantingIntegration {
    public static boolean hasRunes(ItemStack stack) {
        return RuneHelper.countRunes(stack) > 0;
    }
}
