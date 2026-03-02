package insane96mcp.insanesurvivaloverhaul.module.client;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOClientModules;

@LoadFeature(module = ISOClientModules.CLIENT,
        name = "World Border", description = "World border changes")
public class WorldBorder extends Feature {

    @Config(min = 0d, max = 1d, description = "Multiplies the world border transparency by this value")
    public static Double transparency = 0.4d;

    @Config(min = -1f, max = 1f, description = "Multiplies the height of the world border by this value. In vanilla the world border height is dependant on the render distance (in blocks) * 4. Set to 0 to disable.")
    public static Double heightMultiplier = 0.25d;

    @Config(min = 1d, description = "Set the max height of the world border.")
    public static Double capHeight = 128d;

    public static float getTransparencyMultiplier() {
        if (isEnabled(WorldBorder.class))
            return transparency.floatValue();
        return 1f;
    }

    public static boolean shouldShorten() {
        return isEnabled(WorldBorder.class) && heightMultiplier >= 0f;
    }
}
