package insane96mcp.insanesurvivaloverhaul.module.world;

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
@LoadFeature(module = ISOModules.WORLD, description = "Data packs of Nether.")
public class NetherCommon extends Feature {
    @Config(name = "Disable Nether Roof and reduce block ratio", description = "Enables a data pack that makes the nether 128 blocks high instead of 256, effectively disabling the \"Nether Roof\" and reduces the 8 block ratio between nether and other dimensions to 2. This will break if another mod changes the nether dimension")
    public static Boolean netherTweaks = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("nether_tweaks", "Insane's Survival Overhaul Nether Tweaks", () -> this.isEnabled() && !Packs.disableAllDataPacks && netherTweaks);
    }
}
