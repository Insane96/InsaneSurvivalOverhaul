package insane96mcp.insanesurvivaloverhaul.module.farming.plantsgrowth;

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
@LoadFeature(module = ISOModules.FARMING, description = "Data packs of Plants Growth.")
public class PlantsGrowthCommon extends Feature {
	@Config(description = "If true, a data pack is enabled that changes the growth of plants based off various factors, such as sunlight and biome")
	public static Boolean dataPack = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("plant_growth_modifiers", "Insane's Survival Overhaul Plant Growth modifiers", () -> this.isEnabled() && !Packs.disableAllDataPacks && dataPack);
	}
}
