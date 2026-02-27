package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;

@LoadFeature(module = ISOModules.COMBAT, description = "Change armor formula to damage * (1 - 1.5 * (armor / (armor + 20)))")
public class ArmorRework extends Feature {

    //@Config(description = "Vanilla formula is 'damage * (1 - ((MIN(20, MAX(armor / 5, armor - ((4 * damage) / (toughness + 8)))))) / 25))'")
    //public static String formula = "damage * (1 - 1.5 * (armor / (armor + 20)))";

    public static float getCalculatedDamage(float damage, float armor, float toughness) {
        return damage * (1 - 1.5f * (armor / (armor + 20)));
    }
}
