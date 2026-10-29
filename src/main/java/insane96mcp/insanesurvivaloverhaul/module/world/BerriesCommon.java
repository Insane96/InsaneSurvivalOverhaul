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
@LoadFeature(module = ISOModules.WORLD, description = "Data packs of Berries.")
public class BerriesCommon extends Feature {
    @Config(description = """
		Makes sweet berries not plantable, requiring seeds, and also enables a data pack that makes the following changes:
		* Makes sweet berry bushes drop seeds
		* Makes sweet berry patches rarer
		* Makes sweet berry patches have less plants and plants will not be all grown up
	""")
    public static Boolean dataPack = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("berries", "Insane's Survival Overhaul Berries", BerriesCommon::dataPack);
    }

    /**
     * If the berries data pack is enabled
     */
    public static boolean dataPack() {
        return isEnabled(BerriesCommon.class) && !Packs.disableAllDataPacks && dataPack;
    }
}
