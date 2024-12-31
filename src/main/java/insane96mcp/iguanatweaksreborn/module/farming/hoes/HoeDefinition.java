package insane96mcp.iguanatweaksreborn.module.farming.hoes;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.data.IdTagMatcher;
import insane96mcp.insanelib.util.json.ILGsonHelper;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;

@JsonAdapter(HoeDefinition.Serializer.class)
public class HoeDefinition {
	public IdTagMatcher hoe;

	public int scytheRadius;

	public HoeDefinition(IdTagMatcher hoe) {
		this(hoe, 0);
	}

	public HoeDefinition(IdTagMatcher hoe, int scytheRadius) {
		this.hoe = hoe;
		this.scytheRadius = scytheRadius;
	}

	public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<HoeDefinition>>(){}.getType();

	public static class Serializer implements JsonDeserializer<HoeDefinition>, JsonSerializer<HoeDefinition> {
		@Override
		public HoeDefinition deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			IdTagMatcher idTagMatcher = context.deserialize(json.getAsJsonObject().get("hoe"), IdTagMatcher.class);
			return new HoeDefinition(idTagMatcher, GsonHelper.getAsInt(json.getAsJsonObject(), "scythe_radius", 0));
		}

		@Override
		public JsonElement serialize(HoeDefinition src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
			JsonObject jsonObject = new JsonObject();
			ILGsonHelper.add(jsonObject, context, "hoe", src.hoe);
			ILGsonHelper.addProperty(jsonObject, "scythe_radius", src.scytheRadius);
			return jsonObject;
		}
	}
}