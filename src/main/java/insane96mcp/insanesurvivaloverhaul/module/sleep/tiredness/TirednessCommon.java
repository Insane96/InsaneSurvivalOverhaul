package insane96mcp.insanesurvivaloverhaul.module.sleep.tiredness;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.neoforged.fml.config.ModConfig;

@ConfigType(ModConfig.Type.COMMON)
@LoadFeature(module = ISOModules.SLEEP, description = "Resource packs of Tiredness.")
public class TirednessCommon extends Feature {
	@Config(description = "Enables a resource pack that changes the 'Sleeping through this night' message to 'Skipping x hours', showing the hours skipped by sleeping.")
	public static Boolean skippingHoursResourcePack = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addClientPack("skipping_hours_rp", "Insane's Survival Overhaul Skipping Hours", () -> this.isEnabled() && skippingHoursResourcePack);
	}
}
