package insane96mcp.insanesurvivaloverhaul.module.mobs.villager;

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
@LoadFeature(module = ISOModules.MOBS, description = "Data pack of the villagers overhaul. The Villagers feature does nothing when the data pack is disabled.")
public class VillagersCommon extends Feature {
    @Config(description = "Enables the villagers overhaul data pack: abandoned villages, outposts and mansions, emerald ore, emeralds from pillagers and fishing, villager and wandering trader trades. When disabled, the Villagers feature does nothing too.")
    public static Boolean villagersOverhaul = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("villagers_overhaul", "Insane's Survival Overhaul Villagers Overhaul", VillagersCommon::isOverhaulEnabled);
    }

    /**
     * Whether the villagers overhaul data pack is enabled. The overhaul as a whole (Villagers feature included) depends on this.
     */
    public static boolean isOverhaulEnabled() {
        return isEnabled(VillagersCommon.class) && !Packs.disableAllDataPacks && villagersOverhaul;
    }
}
