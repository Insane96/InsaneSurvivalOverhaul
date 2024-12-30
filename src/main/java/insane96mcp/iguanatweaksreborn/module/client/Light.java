package insane96mcp.iguanatweaksreborn.module.client;

import insane96mcp.iguanatweaksreborn.module.ClientModules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;

@Label(name = "Light", description = "Changes to light")
@LoadFeature(module = ClientModules.Ids.CLIENT)
public class Light extends Feature {

    @Config(min = -1)
    @Label(name = "Night Vision Fade out time", description = "How many ticks before expiring Night Vision will slowly fade out? Set to -1 to disable")
    public static Integer nightVisionFadeOutTime = 50;
    @Config(min = -1, max = 1)
    @Label(name = "Force brightness", description = "If enabled, the brightness will be set to this value, ignoring the brightness in video settings.")
    public static Double forceBrightness = 0.15d;
    @Config
    @Label(name = "Force only higher brightness", description = "If true, 'Force brightness' will only apply if the current brightness is higher than 'Force brightness'")
    public static Boolean forceOnlyLowerBrightness = true;

    public Light(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    public static boolean shouldDisableNightVisionFlashing() {
        return isEnabled(Light.class) && nightVisionFadeOutTime > -1;
    }
}
