package insane96mcp.insanesurvivaloverhaul.module.movement.weightedequipment;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.data.ObjTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;

@JsonAdapter(ArmorEnchantmentWeight.Serializer.class)
public class ArmorEnchantmentWeight {
	public ObjTag<Enchantment> enchantment;
	public float percentageSlownessPerLevel = 0f;
	public float flatSlownessPerLevel = 0f;
	public float percentageSlowness = 0f;
	public float flatSlowness = 0f;

	public ArmorEnchantmentWeight(ObjTag<Enchantment> enchantment) {
		this.enchantment = enchantment;
	}

	public static class Builder {
		private final ArmorEnchantmentWeight armorEnchantmentWeight;

		public Builder(ObjTag<Enchantment> enchantment) {
			this.armorEnchantmentWeight = new ArmorEnchantmentWeight(enchantment);
		}

		public Builder setPercentageSlownessPerLevel(float percentageSlownessPerLevel) {
			this.armorEnchantmentWeight.percentageSlowness = percentageSlownessPerLevel;
			return this;
		}

		public Builder setFlatSlownessPerLevel(float flatSlownessPerLevel) {
			this.armorEnchantmentWeight.flatSlownessPerLevel = flatSlownessPerLevel;
			return this;
		}

		public Builder setPercentageSlowness(float percentageSlowness) {
			this.armorEnchantmentWeight.percentageSlowness = percentageSlowness;
			return this;
		}

		public Builder setFlatSlowness(float flatSlowness) {
			this.armorEnchantmentWeight.flatSlowness = flatSlowness;
			return this;
		}

		public ArmorEnchantmentWeight build() {
			return this.armorEnchantmentWeight;
		}
	}

	public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<ArmorEnchantmentWeight>>(){}.getType();

	public static class Serializer implements JsonDeserializer<ArmorEnchantmentWeight>, JsonSerializer<ArmorEnchantmentWeight> {
		@Override
		public ArmorEnchantmentWeight deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jObject = json.getAsJsonObject();
			ObjTag<Enchantment> enchantment = ObjTag.deserialize(jObject.get("enchantment"), Registries.ENCHANTMENT);
			Builder builder = new Builder(enchantment);
			builder.setPercentageSlownessPerLevel(GsonHelper.getAsFloat(jObject, "percentage_slowness_per_level", 0f));
			builder.setFlatSlownessPerLevel(GsonHelper.getAsFloat(jObject, "flat_slowness_per_level", 0f));
			builder.setPercentageSlowness(GsonHelper.getAsFloat(jObject, "percentage_slowness", 0f));
			builder.setFlatSlowness(GsonHelper.getAsFloat(jObject, "flat_slowness", 0f));

			return builder.build();
		}

		@Override
		public JsonElement serialize(ArmorEnchantmentWeight src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("enchantment", context.serialize(src.enchantment));
			if (src.percentageSlownessPerLevel != 0d)
				jsonObject.addProperty("percentage_slowness_per_level", src.percentageSlownessPerLevel);
			if (src.flatSlownessPerLevel != 0d)
				jsonObject.addProperty("flat_slowness_per_level", src.flatSlownessPerLevel);
			if (src.percentageSlowness != 0d)
				jsonObject.addProperty("percentage_slowness", src.percentageSlowness);
			if (src.flatSlowness != 0d)
				jsonObject.addProperty("flat_slowness", src.flatSlowness);

			return jsonObject;
		}
	}
}