package insane96mcp.iguanatweaksreborn.module.client;

import insane96mcp.iguanatweaksreborn.module.ClientModules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;

@Label(name = "World Border", description = "World border changes")
@LoadFeature(module = ClientModules.Ids.CLIENT)
public class WorldBorder extends Feature {

    @Config(min = 0d, max = 1d)
    @Label(name = "World Border Transparency", description = "Multiplies the world border transparency by this value")
    public static Double transparency = 0.4d;

    @Config(min = -1f, max = 1f)
    @Label(name = "Height multiplier", description = "Multiplies the height of the world border by this value. In vanilla the world border height is dependant on the render distance (in blocks) * 4.")
    public static Double shorter = 0.25d;

    @Config(min = 1d)
    @Label(name = "Cap height", description = "Set the max height of the world border.")
    public static Double capHeight = 128d;

    public WorldBorder(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    public static float getTransparencyMultiplier() {
        if (isEnabled(WorldBorder.class))
            return transparency.floatValue();
        return 1f;
    }

    public static boolean shouldShorten() {
        return isEnabled(WorldBorder.class) && shorter >= 0f;
    }
}
