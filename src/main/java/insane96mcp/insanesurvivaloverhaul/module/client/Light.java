package insane96mcp.insanesurvivaloverhaul.module.client;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOClientModules;

@LoadFeature(module = ISOClientModules.CLIENT,
        name = "Light", description = "Changes to light")
public class Light extends Feature {

    @Config(min = -1, description = "How many ticks before expiring Night Vision will slowly fade out? Set to -1 to disable")
    public static Integer nightVisionFadeOutTime = 50;
    @Config(min = 0, max = 1, description = "If enabled, the brightness will be set to this value if higher in the video settings.")
    public static Double forceDarkness = 0.15d;
    @Config(description = "If true, 'Force brightness' will only apply if the current brightness is higher than 'Force brightness'")
    public static Boolean forceOnlyLowerBrightness = true;

    public static boolean shouldDisableNightVisionFlashing() {
        return isEnabled(Light.class) && nightVisionFadeOutTime > -1;
    }
}
