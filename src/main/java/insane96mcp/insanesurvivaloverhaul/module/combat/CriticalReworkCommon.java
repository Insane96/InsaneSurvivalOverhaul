package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import net.neoforged.fml.config.ModConfig;

@ConfigType(ModConfig.Type.COMMON)
@LoadFeature(module = ISOModules.COMBAT, description = "Data packs of Critical Rework.")
public class CriticalReworkCommon extends Feature {
	@Config(description = "Enables a data pack that adds the Critical enchantment (max level V). Each level increases critical chance by 10% and critical damage by 5%. It's incompatible with other damage enchantments (Sharpness, Smite, Bane of Arthropods, Impaling, Density, Breach).")
	public static Boolean criticalEnchantmentDataPack = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("critical_enchantment", "Insane's Survival Overhaul Critical Enchantment", () -> this.isEnabled() && !Packs.disableAllDataPacks && criticalEnchantmentDataPack);
	}
}
