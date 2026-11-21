package insane96mcp.insanesurvivaloverhaul.module.mobs;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;

@LoadFeature(module = ISOModules.MOBS, description = "Makes the Warden easier to summon from Sculk Shriekers.")
public class Warden extends Feature {

    @Config(min = 1, max = 4, description = "Warning level at which a Sculk Shrieker summons the Warden. Vanilla is 4")
    public static Integer summonWarningLevel = 2;

    @Config(min = 0, description = "Ticks after a warning during which the warning level can't increase again. Vanilla is 200")
    public static Integer warningCooldown = 100;

    @Config(min = 1, description = "Ticks without warnings after which the warning level decreases by 1. Vanilla is 12000")
    public static Integer warningDecreaseInterval = 24000;
}
