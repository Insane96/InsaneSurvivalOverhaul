package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.IntegratedPack;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.neoforged.fml.config.ModConfig;

@ConfigType(ModConfig.Type.COMMON)
@LoadFeature(module = ISOModules.COMBAT, description = "Data packs of Snowballs.")
public class SnowballsCommon extends Feature {
	@Config(description = "Enables a data pack that increases snowballs stack size to 64")
	public static Boolean snowballStackSize = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		IntegratedPack.addServerPack(InsaneSO.MOD_ID, "snowball_stack_size", "InsaneLib's Snowball Stack Size", () -> this.isEnabled() && snowballStackSize);
	}
}
