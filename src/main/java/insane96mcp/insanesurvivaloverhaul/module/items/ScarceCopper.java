package insane96mcp.insanesurvivaloverhaul.module.items;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;

@LoadFeature(module = ISOModules.ITEMS, description = "Lowers copper drops.")
public class ScarceCopper extends Feature {
	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("scarce_copper", "Insane's Survival Overhaul Scarce Copper", () -> this.isEnabled() && !Packs.disableAllDataPacks);
	}
}