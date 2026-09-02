package insane96mcp.insanesurvivaloverhaul.module.items.repairkit;

import insane96mcp.experiencetweaks.module.anvil.AnvilMaterialRepair;
import insane96mcp.experiencetweaks.module.anvil.anvilrepair.AnvilBetterRepair;
import insane96mcp.experiencetweaks.module.anvil.anvilrepair.AnvilRepair;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Isolates every reference to Experience Tweaks' classes so RepairKitRepairRecipe never touches them
 * directly: this class is only loaded (and its imports resolved) behind a ModList.isLoaded check.
 */
public class ExperienceTweaksIntegration {

    public record RepairData(float amountRequired, float maxRepair, float costMultiplier) {}

    public static Optional<RepairData> getCustomRepairData(ItemStack repairable, ItemStack material) {
        return AnvilBetterRepair.getCustomAnvilRepair(repairable, material)
                .map(ExperienceTweaksIntegration::toRepairData);
    }

    private static RepairData toRepairData(AnvilRepair.RepairData repairData) {
        return new RepairData(repairData.amountRequired(), repairData.maxRepair(), repairData.costMultiplier());
    }

    public static float getIncreaseMaterialsRequiredWithEnchantments() {
        return AnvilMaterialRepair.getIncreaseMaterialsRequiredWithEnchantments();
    }

    public static float getIncreaseMaterialsRequiredWithEnchantmentsFlat() {
        return AnvilMaterialRepair.getIncreaseMaterialsRequiredWithEnchantmentsFlat();
    }
}
