package insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Type;
import java.util.ArrayList;

@JsonAdapter(AnvilRecipe.Serializer.class)
public class AnvilRecipe {
	public IngredientWithCount leftIngredient;
	public IngredientWithCount rightIngredient;
	public boolean keepDurability;
	public double experience;
	public ItemStack result;

	public AnvilRecipe(IngredientWithCount leftIngredient, IngredientWithCount rightIngredient, boolean keepDurability, double experience, ItemStack result) {
		this.leftIngredient = leftIngredient;
		this.rightIngredient = rightIngredient;
		this.keepDurability = keepDurability;
		this.experience = experience;
		this.result = result;
	}

	public static final Type LIST_TYPE = new TypeToken<ArrayList<AnvilRecipe>>(){}.getType();
	public static class Serializer implements JsonDeserializer<AnvilRecipe> {
		@Override
		public AnvilRecipe deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jObject = json.getAsJsonObject();
			JsonObject jObjectResult = GsonHelper.getAsJsonObject(jObject, "result");
			return new AnvilRecipe(GsonHelper.getAsObject(jObject, "left_ingredient", context, IngredientWithCount.class),
					GsonHelper.getAsObject(jObject, "right_ingredient", context, IngredientWithCount.class),
					GsonHelper.getAsBoolean(jObject, "keep_durability", false),
					GsonHelper.getAsDouble(jObject, "experience", 0f),
					new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(GsonHelper.getAsString(jObjectResult, "id"))), GsonHelper.getAsInt(jObjectResult, "count", 1)));
		}
	}
}
