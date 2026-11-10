package insane96mcp.insanesurvivaloverhaul.module.mobs;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;

@LoadFeature(module = ISOModules.MOBS, description = "Changes to leashes distances.")
public class Leash extends Feature {

    @Config(min = 0, description = "Distance at which leashes break. Vanilla is 10")
    public static Integer maxDistance = 16;

    @Config(min = 0, description = "Distance from which leashed mobs are physically pulled towards the holder. Vanilla is 6")
    public static Integer elasticDistance = 10;
}
