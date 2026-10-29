package insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting;

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
@LoadFeature(module = ISOModules.MINING, description = "Data and resource packs of Anvil Crafting.")
public class AnvilCraftingCommon extends Feature {
	@Config(description = "Enables a data pack that replaces vanilla metal equipment recipes requiring an anvil")
	public static Boolean metalEquipmentInAnvil = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("equipment_forging", "Insane's Survival Overhaul Equipment Forging", () -> this.isEnabled() && metalEquipmentInAnvil && !Packs.disableAllDataPacks);
		InsaneSO.addClientPack("equipment_forging_rp", "Insane's Survival Overhaul Equipment Forging", () -> this.isEnabled() && metalEquipmentInAnvil && !Packs.disableAllDataPacks);
	}
}
