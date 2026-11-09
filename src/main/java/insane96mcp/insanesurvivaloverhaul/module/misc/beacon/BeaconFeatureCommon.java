package insane96mcp.insanesurvivaloverhaul.module.misc.beacon;

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
@LoadFeature(module = ISOModules.MISC, description = "Data packs of the Beacon.")
public class BeaconFeatureCommon extends Feature {
    @Config(description = "Enables a data pack that makes the vanilla Beacon recipe give the Insane's Survival Overhaul Beacon")
    public static Boolean betterBeaconDataPack = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("better_beacon", "Insane's Survival Overhaul Better Beacon", () -> this.isEnabled() && !Packs.disableAllDataPacks && betterBeaconDataPack);
    }
}
