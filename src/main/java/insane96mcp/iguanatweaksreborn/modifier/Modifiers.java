package insane96mcp.iguanatweaksreborn.modifier;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

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
        if (ModList.get().isLoaded("sereneseasons"))
            registerModifier(InsaneSO.location("season"), SeasonModifier.class);
    }

    public static void registerModifier(ResourceLocation id, Type modifier) {
        MODIFIERS.put(id, modifier);
    }
}
