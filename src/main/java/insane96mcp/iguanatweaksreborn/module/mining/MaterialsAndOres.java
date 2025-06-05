package insane96mcp.iguanatweaksreborn.module.mining;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;

@Label(name = "Materials and ores", description = "Various changes for different materials and ores.")
@LoadFeature(module = Modules.Ids.MINING)
public class MaterialsAndOres extends Feature {
	@Config
	@Label(name = "Ore generation Overhaul", description = "Enables a data pack that changes ore generation to be biome based. Less material generate in all the biomes, more in specific biomes.")
	public static Boolean oreGenerationOverhaul = true;
	@Config
	@Label(name = "Disable Ore Veins", description = "https://minecraft.wiki/w/Ore_vein")
	public static Boolean disableOreVeins = true;
	@Config
	@Label(name = "Farmable Iron data pack", description = """
			Enables the following changes to vanilla data pack:
			* Stone (Broken with a non Silk-Touch tool) can drop Iron Nuggets
			* Silverfish can drop Iron Nuggets""")
	public static Boolean farmableIronDataPack = true;

	@Config
	@Label(name = "Ore Smelting Data Pack", description = """
			Enables the following changes to vanilla data pack:
			* Smelting copper in a furnace takes 2x time
			* Smelting Iron in a Furnace takes 4x time, and 2x time in a blast furnace
			* Can no longer smelt gold and Ancient Debris in a Furnace, and 2x in a blast furnace""")
	public static Boolean oreSmelting = true;

	public MaterialsAndOres(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("farmable_iron", "Insane's Survival Overhaul Farmable Iron", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && farmableIronDataPack);
		InsaneSO.addServerPack("ore_smelting", "Insane's Survival Overhaul Ore Smelting", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && oreSmelting);
		InsaneSO.addServerPack("ore_generation", "Insane's Survival Overhaul Ore Generation Overhaul", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && oreGenerationOverhaul);
	}
}
