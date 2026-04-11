package insane96mcp.insanesurvivaloverhaul.module.farming.plantsgrowth;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.data.ObjTag;
import insane96mcp.insanesurvivaloverhaul.data.modifier.Modifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

@JsonAdapter(PlantGrowthMultiplier.Serializer.class)
public class PlantGrowthMultiplier {
	public final ObjTag<Block> block;
	private final float growthMultiplier;
	protected final List<Modifier> modifiers = new ArrayList<>();

	private PlantGrowthMultiplier(ObjTag<Block> block, float growthMultiplier, List<Modifier> modifiers) {
		this.block = block;
		this.growthMultiplier = growthMultiplier;
		this.modifiers.addAll(modifiers);
	}

	/**
	 * Returns >=1 for the chance 1 in this to grow
	 * Values between 0 and 1 have no effect
	 * Returns 0 when the plant will not grow
	 */
	public float getMultiplier(BlockState state, Level level, BlockPos pos) {
		if (!this.block.matches(state.getBlock()))
			return 1f;
        return Modifier.applyModifiers(this.growthMultiplier, this.modifiers, level, pos, null);
	}

	public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<PlantGrowthMultiplier>>(){}.getType();

	public static class Serializer implements JsonDeserializer<PlantGrowthMultiplier> {
		@Override
		public PlantGrowthMultiplier deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jObject = json.getAsJsonObject();
			ObjTag<Block> block = ObjTag.deserialize(jObject.get("block"), Registries.BLOCK);
			float multiplier = GsonHelper.getAsFloat(jObject, "growth_multiplier", 1f);

			List<Modifier> modifiers = Modifier.deserializeList(jObject, "modifiers", context);

			return new PlantGrowthMultiplier(block, multiplier, modifiers);
		}
	}
}