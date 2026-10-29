package insane96mcp.insanesurvivaloverhaul.module.respawn.echopillar;

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
@LoadFeature(module = ISOModules.RESPAWN, description = "Data packs of Echo Pillar.", enabledByDefault = false)
public class EchoPillarCommon extends Feature {
	@Config(description = "Data pack that makes Echo Pillars generate in the world")
	public static Boolean echoPillars = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("echo_pillar", "Insane's Survival Overhaul Echo Pillar", () -> this.isEnabled() && !Packs.disableAllDataPacks && echoPillars);
	}
}
