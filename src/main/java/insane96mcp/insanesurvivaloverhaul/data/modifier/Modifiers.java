package insane96mcp.insanesurvivaloverhaul.data.modifier;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Modifiers {
    public static final Map<ResourceLocation, Type> MODIFIERS = new HashMap<>();

    public static void init() {
        registerModifier(InsaneSO.location("true"), Modifier.class);
        registerModifier(InsaneSO.location("sunlight"), SunlightModifier.class);
        registerModifier(InsaneSO.location("night_time"), NightTimeModifier.class);
        registerModifier(InsaneSO.location("matches_biome"), BiomeModifier.class);
        registerModifier(InsaneSO.location("age"), AgeModifier.class);
        registerModifier(InsaneSO.location("has_been_fed_recently"), FedModifier.class);
    }

    public static void registerModifier(ResourceLocation id, Type modifier) {
        MODIFIERS.put(id, modifier);
    }
}
