package insane96mcp.insanesurvivaloverhaul.module.mining;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;

@LoadFeature(module = ISOModules.MINING, description = "Various changes for different materials and ores.")
public class MaterialsAndOres extends Feature {
	@Config(description = "https://minecraft.wiki/w/Ore_vein")
	public static Boolean disableOreVeins = true;
}
