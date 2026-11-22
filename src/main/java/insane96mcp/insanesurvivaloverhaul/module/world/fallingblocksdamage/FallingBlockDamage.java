package insane96mcp.insanesurvivaloverhaul.module.world.fallingblocksdamage;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.data.ObjTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;

/**
 * Fall damage dealt by a block (or block tag) when it falls on entities
 */
@JsonAdapter(FallingBlockDamage.Serializer.class)
public class FallingBlockDamage {
	public final ObjTag<Block> id;
	public final float damagePerDistance;
	public final int maxDamage;

	public FallingBlockDamage(ObjTag<Block> id, float damagePerDistance, int maxDamage) {
		this.id = id;
		this.damagePerDistance = damagePerDistance;
		this.maxDamage = maxDamage;
	}

	public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<FallingBlockDamage>>(){}.getType();

	public static class Serializer implements JsonDeserializer<FallingBlockDamage>, JsonSerializer<FallingBlockDamage> {
		@Override
		public FallingBlockDamage deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jObject = json.getAsJsonObject();
			ObjTag<Block> id = ObjTag.deserialize(jObject.get("id"), Registries.BLOCK);
			float damagePerDistance = GsonHelper.getAsFloat(jObject, "damage_per_distance");
			if (damagePerDistance < 0f)
				throw new JsonParseException("damage_per_distance must be >= 0 in %s".formatted(json));
			int maxDamage = GsonHelper.getAsInt(jObject, "max_damage");
			if (maxDamage < 0)
				throw new JsonParseException("max_damage must be >= 0 in %s".formatted(json));
			return new FallingBlockDamage(id, damagePerDistance, maxDamage);
		}

		@Override
		public JsonElement serialize(FallingBlockDamage src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("id", src.id.serialize());
			jsonObject.addProperty("damage_per_distance", src.damagePerDistance);
			jsonObject.addProperty("max_damage", src.maxDamage);
			return jsonObject;
		}
	}
}
