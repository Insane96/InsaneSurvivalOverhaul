package insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.lang.reflect.Type;
import java.util.ArrayList;

@JsonAdapter(IngredientWithCount.Serializer.class)
public record IngredientWithCount(Ingredient ingredient, int count) {

	public boolean test(ItemStack stack) {
		return stack.getCount() >= count && ingredient.test(stack);
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, IngredientWithCount> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC, IngredientWithCount::ingredient,
			ByteBufCodecs.VAR_INT, IngredientWithCount::count,
			IngredientWithCount::new
	);

	public static final Type LIST_TYPE = new TypeToken<ArrayList<IngredientWithCount>>() {
	}.getType();

	public static class Serializer implements JsonDeserializer<IngredientWithCount> {
		@Override
		public IngredientWithCount deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonPrimitive())
				return new IngredientWithCount(fromString(json.getAsString()), 1);
			else {
				JsonObject jObject = json.getAsJsonObject();
				return new IngredientWithCount(fromString(GsonHelper.getAsString(jObject, "id")), GsonHelper.getAsInt(jObject, "count", 1));
			}
		}
	}

	private static Ingredient fromString(String s) throws JsonParseException {
		Ingredient ingredient;
		if (s.startsWith("#"))
			ingredient = Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse(s.substring(1))));
		else {
			ResourceLocation itemId = ResourceLocation.parse(s);
			if (!BuiltInRegistries.ITEM.containsKey(itemId))
				throw new JsonParseException("Unknown item '" + itemId + "' for anvil recipe ingredient");
			ingredient = Ingredient.of(BuiltInRegistries.ITEM.get(itemId));
		}
		return ingredient;
	}
}
