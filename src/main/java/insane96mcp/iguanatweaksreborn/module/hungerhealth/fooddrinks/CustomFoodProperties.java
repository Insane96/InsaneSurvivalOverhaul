package insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import insane96mcp.iguanatweaksreborn.data.ISOMobEffectInstance;
import insane96mcp.insanelib.data.IdTagMatcher;
import insane96mcp.insanelib.util.LogHelper;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@JsonAdapter(CustomFoodProperties.Serializer.class)
public class CustomFoodProperties {
	IdTagMatcher food;
	public int nutrition = -1;
	public float saturationModifier = -1f;
	public int eatingTime = -1;
	public Boolean fastEating = null;
	public Boolean canAlwaysEat = null;
	//If null, don't modify existing effects
	public List<Pair<Supplier<ISOMobEffectInstance>, Float>> effects = null;

	public CustomFoodProperties(IdTagMatcher food) {
		this.food = food;
	}

	public List<Pair<ISOMobEffectInstance, Float>> getEffects() {
		return this.effects.stream().map(pair -> Pair.of(pair.getFirst() != null ? pair.getFirst().get() : null, pair.getSecond())).collect(java.util.stream.Collectors.toList());
	}

	@SuppressWarnings("DataFlowIssue")
	public void apply() {
		List<Item> items = FoodDrinks.getAllItems(this.food, false);
		for (Item item : items) {
			if (item.getFoodProperties() == null) {
				LogHelper.warn("Item %s in Custom Food Properties is not edible", item);
				return;
			}
			//noinspection deprecation
			FoodProperties food = item.getFoodProperties();
			if (this.nutrition >= 0)
				food.nutrition = this.nutrition;
			if (this.saturationModifier >= 0f)
				food.saturationModifier = this.saturationModifier;
			if (this.fastEating != null)
				food.fastFood = this.fastEating;
			if (this.canAlwaysEat != null)
				food.canAlwaysEat = this.canAlwaysEat;
		}
	}

	public static class Builder {
		CustomFoodProperties customFoodProperties;

		public Builder(IdTagMatcher food) {
			this.customFoodProperties = new CustomFoodProperties(food);
		}

		public CustomFoodProperties build() {
			return this.customFoodProperties;
		}

		public Builder setNutrition(int nutrition) {
			this.customFoodProperties.nutrition = nutrition;
			return this;
		}

		public Builder setSaturationModifier(float saturationModifier) {
			this.customFoodProperties.saturationModifier = saturationModifier;
			return this;
		}

		public Builder setEatingTime(int eatingTime) {
			this.customFoodProperties.eatingTime = eatingTime;
			return this;
		}

		public Builder fastEating(boolean fastEating) {
			this.customFoodProperties.fastEating = fastEating;
			return this;
		}

		public Builder alwaysEat(boolean alwaysEat) {
			this.customFoodProperties.canAlwaysEat = alwaysEat;
			return this;
		}

		public Builder noEffects() {
			this.customFoodProperties.effects = new ArrayList<>();
			return this;
		}

		public Builder addEffect(ISOMobEffectInstance mobEffectInstance) {
			return this.addEffect(mobEffectInstance, 1f);
		}

		public Builder addEffect(ISOMobEffectInstance mobEffectInstance, float chance) {
			if (this.customFoodProperties.effects == null)
				this.customFoodProperties.effects = new ArrayList<>();
			this.customFoodProperties.effects.add(Pair.of(() -> mobEffectInstance, chance));
			return this;
		}
	}

	@Override
	public String toString() {
		return "FoodValue{food: %s, nutrition: %s, saturation_modifier: %s, eating_time: %s, fast_eating: %s, can_always_eat: %s, effects: %s}".formatted(this.food, this.nutrition, this.saturationModifier, this.eatingTime, this.fastEating, this.canAlwaysEat, this.effects);
	}

	public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<CustomFoodProperties>>(){}.getType();

	public static class Serializer implements JsonDeserializer<CustomFoodProperties>, JsonSerializer<CustomFoodProperties> {
		@Override
		public CustomFoodProperties deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jObject = json.getAsJsonObject();
			IdTagMatcher food = context.deserialize(jObject.get("food"), IdTagMatcher.class);

			Builder builder = new Builder(food)
					.setNutrition(GsonHelper.getAsInt(jObject, "nutrition", -1))
					.setSaturationModifier(GsonHelper.getAsFloat(jObject, "saturation_modifier", -1f))
					.setEatingTime(GsonHelper.getAsInt(jObject, "eating_time", -1));
			if (jObject.has("fast_eating"))
				builder.fastEating(GsonHelper.getAsBoolean(jObject, "fast_eating"));
			if (jObject.has("can_always_eat"))
				builder.alwaysEat(GsonHelper.getAsBoolean(jObject, "can_always_eat"));
			if (jObject.has("effects")) {
				JsonArray jArray = jObject.getAsJsonArray("effects");
				if (jArray.isEmpty())
					builder.noEffects();
				else
					jArray.forEach(element -> {
						float chance = 1f;
						if (element.getAsJsonObject().has("chance"))
							chance = GsonHelper.getAsFloat(element.getAsJsonObject(), "chance");
                        builder.addEffect(context.deserialize(element, ISOMobEffectInstance.class), chance);
                    });
			}

			return builder.build();
		}

		@Override
		public JsonElement serialize(CustomFoodProperties src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("food", context.serialize(src.food));
			if (src.nutrition >= 0)
				jsonObject.addProperty("nutrition", src.nutrition);
			if (src.saturationModifier >= 0f)
				jsonObject.addProperty("saturation_modifier", src.saturationModifier);
			if (src.eatingTime >= 0)
				jsonObject.addProperty("eating_time", src.eatingTime);
			if (src.fastEating != null)
				jsonObject.addProperty("fast_eating", src.fastEating);
			if (src.canAlwaysEat != null)
				jsonObject.addProperty("can_always_eat", src.canAlwaysEat);
			if (src.effects != null) {
				JsonArray effects = new JsonArray();
				src.getEffects().forEach(pair -> {
					JsonElement effect = context.serialize(pair.getFirst());
					if (pair.getSecond() != 1f)
						((JsonObject)effect).addProperty("chance", pair.getSecond());
					effects.add(effect);
				});
				jsonObject.add("effects", effects);
			}

			return jsonObject;
		}
	}
}
