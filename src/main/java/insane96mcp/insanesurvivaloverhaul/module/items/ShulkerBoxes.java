package insane96mcp.insanesurvivaloverhaul.module.items;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;

@LoadFeature(module = ISOModules.ITEMS, description = "Changes to Shulker Boxes.")
public class ShulkerBoxes extends Feature {

	@Config(description = "Enables a data pack that changes Shulker Boxes recipe to use cloth (or leather if the Cloth feature is disabled) and chests instead of shulker shells, and shulkers will drop Shulker Boxes directly instead of shells.")
	public static Boolean earlyGameShulkerBoxes = true;

	@Config(description = "Enables a resource pack that renames Shulker Boxes to Sack")
	public static Boolean renameShulkerBoxToSack = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("early_shulker_boxes", "Insane's Survival Overhaul Early Shulker Box", () -> this.isEnabled() && !Packs.disableAllDataPacks && earlyGameShulkerBoxes);
		InsaneSO.addClientPack("early_shulker_boxes_rp", "Insane's Survival Overhaul Sack", () -> this.isEnabled() && earlyGameShulkerBoxes && renameShulkerBoxToSack);
	}
}
