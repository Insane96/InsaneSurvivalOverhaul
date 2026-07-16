package insane96mcp.insanesurvivaloverhaul.module.mining;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;

@LoadFeature(module = ISOModules.MINING, description = "Various changes for different materials and ores.")
public class MaterialsAndOres extends Feature {
	@Config(description = "https://minecraft.wiki/w/Ore_vein")
	public static Boolean disableOreVeins = true;
	@Config(description = "Enables a data pack that removes the vanilla feature of discarding ores if exposed to air. This applies to: coal, diamonds, gold and lapis")
	public static Boolean oreGenerationOverhaul = true;
	@Config(description = """
			Enables a data pack with the following changes:
			* Stone (Broken with a non Silk-Touch tool) can drop Iron Nuggets
			* Silverfish can drop Iron Nuggets""")
	public static Boolean farmableIronDataPack = false;

	@Config(description = """
			Enables the following changes to vanilla data pack:
			* Smelting Raw Copper and Raw Iron takes 2x time
			* Blasting raw minerals takes 1x time
			* Blasting ores now takes 2x time but yields 2x materials from minerals (2x the normal drops without Fortune), and yields 3x raw minerals for iron, gold and copper
			* You can no longer smelt anything other than Raw Copper and Raw Iron. A Blast Furnace is required
			* Heavily increased experience from smelting ores
			""")
	public static Boolean oreSmelting = true;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("farmable_iron", "Insane's Survival Overhaul Farmable Iron", () -> this.isEnabled() && !Packs.disableAllDataPacks && farmableIronDataPack);
		InsaneSO.addServerPack("ore_smelting", "Insane's Survival Overhaul Ore Smelting", () -> this.isEnabled() && !Packs.disableAllDataPacks && oreSmelting);
		InsaneSO.addServerPack("ore_generation", "Insane's Survival Overhaul Ore Generation Overhaul", () -> this.isEnabled() && !Packs.disableAllDataPacks && oreGenerationOverhaul);
	}
}
