package insane96mcp.insanesurvivaloverhaul.module.farming.bonemeal;

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
@LoadFeature(module = ISOModules.FARMING, description = "Data packs of Bone Meal.")
public class BoneMealCommon extends Feature {
    @Config(description = "Makes Cave Vines, Saplings, Sweet Berry Bushes and Crops requiring more bone meal. Makes rotten flesh compostable. Makes crafting Bones yield less Bone Meal and Bone Blocks cheaper.")
    public static Boolean dataPack = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("bone_meal", "Insane's Survival Overhaul Bone Meal", () -> this.isEnabled() && !Packs.disableAllDataPacks && dataPack);
    }
}
