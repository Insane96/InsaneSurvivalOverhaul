package insane96mcp.insanesurvivaloverhaul.data.modifier;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Modifiers {
    public static final Map<ResourceLocation, Type> MODIFIERS = new HashMap<>();

    public static void init() {
        registerModifier(InsaneSO.id("true"), Modifier.class);
        registerModifier(InsaneSO.id("sunlight"), SunlightModifier.class);
        registerModifier(InsaneSO.id("night_time"), NightTimeModifier.class);
        registerModifier(InsaneSO.id("matches_biome"), BiomeModifier.class);
        registerModifier(InsaneSO.id("age"), AgeModifier.class);
        registerModifier(InsaneSO.id("has_been_fed_recently"), FedModifier.class);
    }

    public static void registerModifier(ResourceLocation id, Type modifier) {
        MODIFIERS.put(id, modifier);
    }
}
