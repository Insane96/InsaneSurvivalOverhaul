package insane96mcp.insanesurvivaloverhaul.module.misc;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;

@LoadFeature(module = ISOModules.MISC, description = "Resource and data packs")
public class Packs extends Feature {

    @Config(description = "If true, no integrated data pack will be loaded")
    public static Boolean disableAllDataPacks = false;

    @Config(description = "If true a data pack will be enabled that disables villages and pillagers outpost generation.")
    public static Boolean disableLongNosesStructures = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("disable_long_noses", "Insane's Survival Overhaul Disable Long Noses", () -> this.isEnabled() && !Packs.disableAllDataPacks && disableLongNosesStructures);
    }
}
