package insane96mcp.iguanatweaksreborn.module.misc.beaconconduit;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.data.IdTagMatcher;
import insane96mcp.insanelib.util.LogHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonAdapter(BeaconEffect.Serializer.class)
public class BeaconEffect extends IdTagMatcher {
    int[] timeCost;
    int heightRequired;

    public BeaconEffect(String location, int[] timeCost, int heightRequired) {
        super(Type.ID, ResourceLocation.parse(location), null);
        this.timeCost = timeCost;
        this.heightRequired = heightRequired;
    }

    public BeaconEffect(MobEffect mobEffect, int[] timeCost, int heightRequired) {
        super(Type.ID, Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getKey(mobEffect)), null);
        this.timeCost = timeCost;
        this.heightRequired = heightRequired;
    }

    public BeaconEffect(String location, int[] timeCost) {
        this(location, timeCost, 1);
    }

    public BeaconEffect(MobEffect mobEffect, int[] timeCost) {
        this(mobEffect, timeCost, 1);
    }

    @Nullable
    public MobEffect getEffect() {
        if (!ForgeRegistries.MOB_EFFECTS.containsKey(this.location)) {
            LogHelper.warn("No mob effect found with id %s", this.location);
            return null;
        }
        return ForgeRegistries.MOB_EFFECTS.getValue(this.location);
    }

    public int getMaxAmplifier() {
        return this.timeCost.length - 1;
    }

    public int getHeightRequired() {
        return this.heightRequired;
    }

    public int getTimeCostForAmplifier(int amplifier) {
        if (this.timeCost.length <= amplifier)
            return 1;
        return this.timeCost[amplifier];
    }

    public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<BeaconEffect>>() {
    }.getType();

    static class Serializer implements JsonSerializer<BeaconEffect>, JsonDeserializer<BeaconEffect> {
        @Override
        public BeaconEffect deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            int heightRequired = GsonHelper.getAsInt(jObject, "height_required", 1);
            JsonArray jArray = jObject.getAsJsonArray("time_cost");
            if (jArray.size() > 8)
                throw new JsonParseException("time_cost size cannot be greater than 8");
            List<Integer> timeCost = new ArrayList<>();
            jArray.forEach(jsonElement -> timeCost.add(jsonElement.getAsInt()));
            return new BeaconEffect(GsonHelper.getAsString(jObject, "id"), timeCost.stream().mapToInt(i -> i).toArray(), heightRequired);
        }

        @Override
        public JsonElement serialize(BeaconEffect src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.addProperty("id", src.location.toString());
            if (src.heightRequired > 1)
                jObject.addProperty("height_required", src.heightRequired);
            jObject.addProperty("id", src.location.toString());
            JsonArray jArray = new JsonArray();
            for (int i = 0; i < src.timeCost.length; i++) {
                jArray.add(src.timeCost[i]);
            }
            jObject.add("time_cost", jArray);
            return jObject;
        }
    }
}
