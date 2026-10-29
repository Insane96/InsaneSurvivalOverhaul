package insane96mcp.insanesurvivaloverhaul.module.world.spawners;

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
@LoadFeature(module = ISOModules.WORLD, description = "Data packs of Spawners.")
public class SpawnersCommon extends Feature {
	@Config(description = "If true, spawners will generate in an empowered state. When empowered, will generate mobs really fast for a while and then will disable. This is not compatible with disable spawners.")
	public static Boolean empowered$enabled = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("empowered_spawners", "Insane's Survival Overhaul Empowered Spawners", SpawnersCommon::empowered);
	}

	/**
	 * If the empowered spawners data pack is enabled
	 */
	public static boolean empowered() {
		return isEnabled(SpawnersCommon.class) && !Packs.disableAllDataPacks && empowered$enabled;
	}
}
