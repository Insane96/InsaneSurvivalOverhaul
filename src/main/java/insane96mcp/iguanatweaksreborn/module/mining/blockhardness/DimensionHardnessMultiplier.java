package insane96mcp.iguanatweaksreborn.module.mining.blockhardness;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;

@JsonAdapter(DimensionHardnessMultiplier.Serializer.class)
public class DimensionHardnessMultiplier {
    public final ResourceLocation dimension;
    public final double multiplier;

    public DimensionHardnessMultiplier(String dimension, double multiplier) {
        this.dimension = ResourceLocation.parse(dimension);
        this.multiplier = multiplier;
    }

    public DimensionHardnessMultiplier(ResourceLocation dimension, double multiplier) {
        this.dimension = dimension;
        this.multiplier = multiplier;
    }

    public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<DimensionHardnessMultiplier>>(){}.getType();

    public static class Serializer implements JsonDeserializer<DimensionHardnessMultiplier>, JsonSerializer<DimensionHardnessMultiplier> {
        @Override
        public DimensionHardnessMultiplier deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String dimension = GsonHelper.getAsString(json.getAsJsonObject(), "dimension");
            if (!ResourceLocation.isValidResourceLocation(dimension))
                throw new JsonParseException("Invalid dimension: %s".formatted(dimension));

            return new DimensionHardnessMultiplier(dimension, GsonHelper.getAsDouble(json.getAsJsonObject(), "multiplier"));
        }

        @Override
        public JsonElement serialize(DimensionHardnessMultiplier src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("dimension", src.dimension.toString());
            jsonObject.addProperty("multiplier", src.multiplier);
            return jsonObject;
        }
    }
}