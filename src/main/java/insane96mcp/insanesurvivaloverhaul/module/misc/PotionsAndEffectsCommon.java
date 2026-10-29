package insane96mcp.insanesurvivaloverhaul.module.misc;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.IntegratedPack;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.neoforged.fml.config.ModConfig;

@ConfigType(ModConfig.Type.COMMON)
@LoadFeature(module = ISOModules.MISC, description = "Data packs of Potions and Effects.")
public class PotionsAndEffectsCommon extends Feature {
    @Config(description = "Enables a data pack that increases potions stack sizes")
    public static Boolean potionStackSize = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        IntegratedPack.addServerPack(InsaneSO.MOD_ID, "potion_stack_size", "InsaneLib's Potion Stack Size", () -> this.isEnabled() && potionStackSize);
    }
}
