package insane96mcp.insanesurvivaloverhaul.module.farming.crops;

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
@LoadFeature(module = ISOModules.FARMING, description = "Data packs of Crops.")
public class CropsCommon extends Feature {
	@Config(description = """
		Makes potatoes and carrots not plantable and also enables a data pack that makes the following changes:
		* Makes all vanilla crops drop only one seed (and makes carrots and potatoes drop the new seed item)
		* Makes crops hard to break, requiring an hoe as correct tool
		* Makes melon seeds and pumpkin seeds harder to obtain
		* Removes carrots and potato drops from zombies
		* Removes wheat seeds from tall grass
		* Makes wild crops generate in the world""")
	public static Boolean dataPack = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("crops", "Insane's Survival Overhaul Crops", CropsCommon::dataPack);
	}

	/**
	 * If the crops data pack is enabled
	 */
	public static boolean dataPack() {
		return isEnabled(CropsCommon.class) && !Packs.disableAllDataPacks && dataPack;
	}
}
