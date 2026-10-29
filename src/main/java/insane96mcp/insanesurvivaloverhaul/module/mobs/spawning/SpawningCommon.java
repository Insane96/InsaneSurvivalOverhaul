package insane96mcp.insanesurvivaloverhaul.module.mobs.spawning;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.neoforged.fml.config.ModConfig;

@ConfigType(ModConfig.Type.COMMON)
@LoadFeature(module = ISOModules.MOBS, description = "Data packs of Spawning.")
public class SpawningCommon extends Feature {
    @Config(description = "Enables a data pack that makes Guardians spawn in deep oceans. These guardians have much less health compared to monument guardians and are smaller.")
    public static Boolean guardiansInDeepOceans = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("ocean_guardians", "Insane's Survival Overhaul Ocean Guardians", SpawningCommon::guardiansInDeepOceans);
    }

    /**
     * If the ocean guardians data pack is enabled
     */
    public static boolean guardiansInDeepOceans() {
        return isEnabled(SpawningCommon.class) && /*!Packs.disableAllDataPacks &&*/ guardiansInDeepOceans;
    }
}
