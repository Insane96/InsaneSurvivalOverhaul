package insane96mcp.insanesurvivaloverhaul.module.world;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;

@LoadFeature(module = ISOModules.WORLD, canBeDisabled = false)
public class DayNightCycle extends Feature {
    @Config(description = "How many minutes does a full day last? This DOESN'T CHANGE the day night cycle duration, but it's only used for calculations in case you change it with another mod.")
    public static Double dayNightCycleDuration = 20d;

    /**
     * Returns the ratio at which the day night cycle is set compared to vanilla.
     */
    public static double getDurationMultiplier() {
        return dayNightCycleDuration / 20d;
    }
}