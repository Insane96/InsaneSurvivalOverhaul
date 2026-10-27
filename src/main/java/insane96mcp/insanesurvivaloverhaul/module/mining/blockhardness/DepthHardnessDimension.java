package insane96mcp.insanesurvivaloverhaul.module.mining.blockhardness;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;

/**
 * In this case the {@link DimensionHardnessMultiplier#multiplier} field is added per block below the {@link DepthHardnessDimension#applyBelowY} level
 */
@JsonAdapter(DepthHardnessDimension.Serializer.class)
public class DepthHardnessDimension extends DimensionHardnessMultiplier {

	public final int applyBelowY;
	public final int stopAt;

	public DepthHardnessDimension(ResourceLocation dimension, double multiplier, int applyBelowY, int stopAt) {
		super(dimension, multiplier);
		this.applyBelowY = applyBelowY;
		this.stopAt = stopAt;
	}

	public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<DepthHardnessDimension>>(){}.getType();

	public static class Serializer implements JsonDeserializer<DepthHardnessDimension>, JsonSerializer<DepthHardnessDimension> {
		@Override
		public DepthHardnessDimension deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			return new DepthHardnessDimension(parseDimension(json), GsonHelper.getAsDouble(jsonObject, "multiplier"), GsonHelper.getAsInt(jsonObject, "apply_below_y", Integer.MAX_VALUE), GsonHelper.getAsInt(jsonObject, "stop_at", Integer.MIN_VALUE));
		}

		@Override
		public JsonElement serialize(DepthHardnessDimension src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("dimension", src.dimension.toString());
			jsonObject.addProperty("multiplier", src.multiplier);
			if (src.applyBelowY != Integer.MAX_VALUE)
				jsonObject.addProperty("apply_below_y", src.applyBelowY);
			if (src.stopAt != Integer.MIN_VALUE)
				jsonObject.addProperty("stop_at", src.stopAt);

			return jsonObject;
		}
	}
}
