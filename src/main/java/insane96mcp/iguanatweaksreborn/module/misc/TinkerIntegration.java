package insane96mcp.iguanatweaksreborn.module.misc;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;

@Label(name = "Tinker Integration", description = "Various data packs that can be enabled/disabled")
@LoadFeature(module = Modules.Ids.MISC)
public class TinkerIntegration extends Feature {

    @Config(min = 0d, max = 128d)
    @Label(description = "Multiplier applied to tinkers construct heads and modifiers mining speed. This also applies to additive modifiers")
    public static Double miningSpeedModifier = 0.75d;

    @Config(min = 0d, max = 128d)
    @Label(description = "Multiplier applied to tinkers construct heads and modifiers attack damage. This also applies to additive modifiers")
    public static Double damageModifier = 0.75d;

    @Config(min = 0d, max = 128d)
    @Label(description = "Multiplier applied to tinkers construct heads and modifiers durability. This also applies to additive modifiers")
    public static Double durabilityModifier = 0.75d;

    public TinkerIntegration(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    public static float miningSpeedModifier() {
        return Feature.isEnabled(TinkerIntegration.class) ? miningSpeedModifier.floatValue() : 1f;
    }

    public static float damageModifier() {
        return Feature.isEnabled(TinkerIntegration.class) ? damageModifier.floatValue() : 1f;
    }

    public static float durabilityModifier() {
        return Feature.isEnabled(TinkerIntegration.class) ? durabilityModifier.floatValue() : 1f;
    }
}
