package insane96mcp.iguanatweaksreborn.module.mobs.spawning;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.util.json.ILGsonHelper;
import insane96mcp.insanelib.util.json.validator.IntMinMaxValidator;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonChangedEvent;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.init.ModConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@LoadFeature(module = Modules.Ids.MOBS, requiresMods = {"sereneseasons"}, description = "Changes to mob spawn with Serene Seasons installed. Check this feature's json config folder for all the configuration options")
public class SeasonSpawning extends JsonFeature {
    public static final ArrayList<SeasonSettings> SEASON_SETTINGS_DEFAULT = new ArrayList<>(List.of(
            new SeasonSettings(List.of(Season.SubSeason.EARLY_WINTER, Season.SubSeason.MID_WINTER, Season.SubSeason.LATE_WINTER), 112, 70, 0, 0),
            new SeasonSettings(List.of(Season.SubSeason.EARLY_SPRING, Season.SubSeason.MID_SPRING, Season.SubSeason.LATE_SPRING), 128, 70, 30, 20),
            new SeasonSettings(List.of(Season.SubSeason.EARLY_SUMMER, Season.SubSeason.MID_SUMMER, Season.SubSeason.LATE_SUMMER), 128, 55, 50, 20),
            new SeasonSettings(List.of(Season.SubSeason.EARLY_AUTUMN, Season.SubSeason.MID_AUTUMN, Season.SubSeason.LATE_AUTUMN), 128, 70, 15, 10)
    ));
    public static final ArrayList<SeasonSettings> seasonSettings = new ArrayList<>();

    public SeasonSpawning(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        JSON_CONFIGS.add(new JsonConfig<>("season_settings.json", seasonSettings, SEASON_SETTINGS_DEFAULT, SeasonSettings.LIST_TYPE));
    }

    @Override
    public String getModConfigFolder() {
        return InsaneSO.CONFIG_FOLDER;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && ModList.get().isLoaded("sereneseasons");
    }

    //TODO this will not work if any other dimension uses seasons
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!this.isEnabled())
            return;

        tryApply(seasonSettings, SeasonHelper.getSeasonState(event.getServer().overworld()).getSubSeason());
    }

    public static void onSeasonChanged(SeasonChangedEvent.Standard event) {
        if (!Feature.isEnabled(SeasonSpawning.class)
                || !ModConfig.seasons.isDimensionWhitelisted(event.getLevel().dimension()))
            return;

        tryApply(seasonSettings, event.getNewSeason());
    }

    public static void tryApply(List<SeasonSettings> seasonSettings, Season.SubSeason subSeason) {
        seasonSettings.forEach(seasonSetting -> seasonSetting.tryApply(subSeason));
    }

    @JsonAdapter(SeasonSettings.Serializer.class)
    public static class SeasonSettings {
        public List<Season.SubSeason> seasons;
        @Nullable
        public Integer despawnDistance;
        @Nullable
        public Integer hostileCap;
        @Nullable
        public Integer animalCap;
        @Nullable
        public Integer waterAmbientCap;

        public SeasonSettings(List<Season.SubSeason> seasons, @Nullable Integer despawnDistance, @Nullable Integer hostileCap, @Nullable Integer animalCap, @Nullable Integer waterAmbientCap) {
            this.seasons = seasons;
            this.despawnDistance = despawnDistance;
            this.hostileCap = hostileCap;
            this.animalCap = animalCap;
            this.waterAmbientCap = waterAmbientCap;
        }

        public void tryApply(Season.SubSeason subSeason) {
            if (seasons.contains(subSeason))
                this.apply();
        }

        public void apply() {
			MobCategory.MONSTER.despawnDistance = Objects.requireNonNullElse(despawnDistance, 128);
            MobCategory.MONSTER.max = Objects.requireNonNullElse(hostileCap, 70);
            MobCategory.CREATURE.max = Objects.requireNonNullElse(animalCap, 10);
            MobCategory.WATER_AMBIENT.max = Objects.requireNonNullElse(waterAmbientCap, 20);
        }

        public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<SeasonSettings>>(){}.getType();

        public static class Serializer implements JsonDeserializer<SeasonSettings>, JsonSerializer<SeasonSettings> {
            @Override
            public SeasonSettings deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                List<Season.SubSeason> subSeasons = new ArrayList<>();
                JsonArray aSeasons = GsonHelper.getAsJsonArray(json.getAsJsonObject(), "seasons");
                if (aSeasons.isEmpty())
                    throw new JsonParseException("seasons list must contain at least one entry");
                for (JsonElement jsonElement : aSeasons)
                    subSeasons.add(Season.SubSeason.valueOf(jsonElement.getAsString().toUpperCase(Locale.ROOT)));
                Integer despawnDistance = ILGsonHelper.getAsNullableInt(json.getAsJsonObject(), "despawn_distance", IntMinMaxValidator.atLeast(0));
                Integer hostileCap = ILGsonHelper.getAsNullableInt(json.getAsJsonObject(), "hostile_cap", IntMinMaxValidator.atLeast(0));
                Integer animalCap = ILGsonHelper.getAsNullableInt(json.getAsJsonObject(), "animal_cap", IntMinMaxValidator.atLeast(0));
                Integer waterAmbientCap = ILGsonHelper.getAsNullableInt(json.getAsJsonObject(), "water_ambient_cap", IntMinMaxValidator.atLeast(0));
                return new SeasonSettings(subSeasons, despawnDistance, hostileCap, animalCap, waterAmbientCap);
            }

            @Override
            public JsonElement serialize(SeasonSettings src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();
                JsonArray aSeasons = new JsonArray();
                for (Season.SubSeason subSeason : src.seasons)
                    aSeasons.add(subSeason.name().toLowerCase(Locale.ROOT));
                jsonObject.add("seasons", aSeasons);
                if (src.despawnDistance != null)
                    jsonObject.addProperty("despawn_distance", src.despawnDistance);
                if (src.hostileCap != null)
                    jsonObject.addProperty("hostile_cap", src.hostileCap);
                if (src.animalCap != null)
                    jsonObject.addProperty("animal_cap", src.animalCap);
                if (src.waterAmbientCap != null)
                    jsonObject.addProperty("water_ambient_cap", src.waterAmbientCap);
                return jsonObject;
            }
        }
    }
}