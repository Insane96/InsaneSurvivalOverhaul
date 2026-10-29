package insane96mcp.insanesurvivaloverhaul.module.world.coalfire;

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
@LoadFeature(module = ISOModules.WORLD, description = "Data packs of Coal & Fire.")
public class CoalFireCommon extends Feature {
    @Config(description = """
			If enabled, a data pack will be enabled that:
			* Removes the Charcoal recipe from smelting
			* Makes Coal Ore require an Iron Pickaxe or better to mine
			* Replaces Flint and Steel in the overworld with firestarter
			* Changes flint and steel recipe to require blaze powder""")
    public static Boolean noCharcoalSmeltingAndIronCoal = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("coal_fire", "Insane's Survival Overhaul No Charcoal Smelting and Iron Coal", () -> this.isEnabled() && !Packs.disableAllDataPacks && noCharcoalSmeltingAndIronCoal);
    }
}
