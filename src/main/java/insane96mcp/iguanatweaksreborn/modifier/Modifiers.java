package insane96mcp.iguanatweaksreborn.modifier;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Modifiers {
    public static final Map<ResourceLocation, Type> MODIFIERS = new HashMap<>();

    public static void init() {
        registerModifier(new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "true"), Modifier.class);
        registerModifier(new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "sunlight"), SunlightModifier.class);
        registerModifier(new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "night_time"), NightTimeModifier.class);
        registerModifier(new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "matches_biome"), BiomeModifier.class);
        registerModifier(new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "age"), AgeModifier.class);
        registerModifier(new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "has_been_fed_recently"), FedModifier.class);
        if (ModList.get().isLoaded("sereneseasons"))
            registerModifier(new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "season"), SeasonModifier.class);
    }

    public static Type registerModifier(ResourceLocation id, Type modifier) {
        MODIFIERS.put(id, modifier);
        return modifier;
    }
}
