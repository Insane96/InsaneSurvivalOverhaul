package insane96mcp.insanesurvivaloverhaul.module.items.shulkerboxes;

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
@LoadFeature(module = ISOModules.ITEMS, description = "Data and resource packs of Shulker Boxes.")
public class ShulkerBoxesCommon extends Feature {
	@Config(description = "Enables a data pack that makes Shulker Boxes obtainable before the End: they're crafted with Nether planks, a Blaze Rod and two Pouches (two chests if the Pouch feature is disabled) instead of shulker shells, keeping the Pouches' contents, and shulkers drop Shulker Boxes directly instead of shells.")
	public static Boolean earlyGameShulkerBoxes = true;

	@Config(description = "Enables a resource pack that renames Shulker Boxes to Nether Pouch. Only active if 'Early game shulker boxes' is enabled.")
	public static Boolean renameShulkerBoxToNetherPouch = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("early_shulker_boxes", "Insane's Survival Overhaul Early Shulker Box", () -> this.isEnabled() && !Packs.disableAllDataPacks && earlyGameShulkerBoxes);
		InsaneSO.addClientPack("early_shulker_boxes_rp", "Insane's Survival Overhaul Nether Pouch", () -> this.isEnabled() && earlyGameShulkerBoxes && renameShulkerBoxToNetherPouch);
	}
}
